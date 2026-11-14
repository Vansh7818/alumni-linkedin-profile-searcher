package com.alumnisearch.scraper;

import com.alumnisearch.config.PhantomBusterProperties;
import com.alumnisearch.dto.external.PhantomBusterContainerResponse;
import com.alumnisearch.dto.external.PhantomBusterLaunchRequest;
import com.alumnisearch.dto.external.PhantomBusterLaunchResponse;
import com.alumnisearch.exception.ExternalServiceException;
import com.alumnisearch.util.AlumniDataMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhantomBusterScraperService implements LinkedInScraperStrategy {

    private final PhantomBusterProperties properties;
    private final RestClient restClient;
    private final AlumniDataMapper alumniDataMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderName() {
        return "PhantomBuster";
    }

    @Override
    public boolean isAvailable() {
        return properties.isConfigured() && !properties.getMockMode().isEnabled();
    }

    @Override
    public List<ScrapedAlumniRecord> scrape(String university, String designation, Integer passoutYear) {
        if (!isAvailable()) {
            throw new ExternalServiceException("PhantomBuster API is not configured or mock mode is enabled");
        }

        log.info("Launching PhantomBuster LinkedIn scraper for University: '{}', Designation: '{}', Year: '{}'",
                university, designation, passoutYear);

        String searchQuery = buildLinkedInSearchQuery(university, designation, passoutYear);
        String containerId = launchAgent(searchQuery);

        log.info("PhantomBuster agent launched successfully with containerId: {}", containerId);

        List<Map<String, Object>> rawResults = pollContainerResults(containerId);

        log.info("PhantomBuster returned {} raw profile records", rawResults.size());

        List<ScrapedAlumniRecord> records = new ArrayList<>();
        for (Map<String, Object> rawItem : rawResults) {
            ScrapedAlumniRecord record = alumniDataMapper.fromRawMap(rawItem, university, designation, passoutYear);
            if (record != null) {
                records.add(record);
            }
        }

        return records;
    }

    private String buildLinkedInSearchQuery(String university, String designation, Integer passoutYear) {
        StringBuilder query = new StringBuilder();
        query.append("\"").append(university.trim()).append("\"");
        query.append(" AND \"").append(designation.trim()).append("\"");
        if (passoutYear != null) {
            query.append(" AND \"").append(passoutYear).append("\"");
        }
        return query.toString();
    }

    private String launchAgent(String searchQuery) {
        Map<String, Object> argument = new HashMap<>();
        argument.put("searches", searchQuery);
        argument.put("category", "People");
        argument.put("numberOfResultsPerSearch", 10);

        PhantomBusterLaunchRequest request = PhantomBusterLaunchRequest.builder()
                .id(properties.getAgentId())
                .argument(argument)
                .build();

        try {
            PhantomBusterLaunchResponse response = restClient.post()
                    .uri("/agents/launch")
                    .header("X-Phantombuster-Key", properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PhantomBusterLaunchResponse.class);

            if (response == null || response.getContainerId() == null) {
                String errMsg = response != null && response.getMessage() != null
                        ? response.getMessage()
                        : "No containerId returned by PhantomBuster";
                throw new ExternalServiceException("Failed to launch PhantomBuster agent: " + errMsg);
            }

            return response.getContainerId();
        } catch (RestClientResponseException ex) {
            log.error("PhantomBuster launch error: HTTP {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ExternalServiceException("PhantomBuster API error during launch: " + ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error launching PhantomBuster agent: {}", ex.getMessage(), ex);
            throw new ExternalServiceException("Failed to communicate with PhantomBuster API: " + ex.getMessage(), ex);
        }
    }

    private List<Map<String, Object>> pollContainerResults(String containerId) {
        long startTime = System.currentTimeMillis();
        long maxWaitMillis = (long) properties.getMaxWaitSeconds() * 1000;

        while ((System.currentTimeMillis() - startTime) < maxWaitMillis) {
            try {
                Thread.sleep(properties.getPollIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PhantomBuster execution was interrupted");
            }

            PhantomBusterContainerResponse container = fetchContainerStatus(containerId);
            if (container == null) {
                continue;
            }

            String status = container.getContainerStatus();
            log.debug("Polling container {} status: {}", containerId, status);

            if ("finished".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
                return extractResultObject(container);
            }

            if ("error".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status)) {
                throw new ExternalServiceException("PhantomBuster container failed with status: " + status +
                        ", output: " + container.getOutput());
            }
        }

        // Attempt fallback fetch from agent result object before failing
        List<Map<String, Object>> fallback = fetchAgentResultObject();
        if (!fallback.isEmpty()) {
            return fallback;
        }

        throw new ExternalServiceException("Timed out waiting for PhantomBuster container " + containerId +
                " to finish after " + properties.getMaxWaitSeconds() + " seconds");
    }

    private PhantomBusterContainerResponse fetchContainerStatus(String containerId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/containers/fetch")
                            .queryParam("id", containerId)
                            .build())
                    .header("X-Phantombuster-Key", properties.getApiKey())
                    .retrieve()
                    .body(PhantomBusterContainerResponse.class);
        } catch (Exception ex) {
            log.warn("Error checking container status: {}", ex.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> fetchAgentResultObject() {
        try {
            Map<String, Object> resp = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/agents/fetch-result-object")
                            .queryParam("id", properties.getAgentId())
                            .build())
                    .header("X-Phantombuster-Key", properties.getApiKey())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (resp != null && resp.containsKey("resultObject")) {
                return parseRawResult(resp.get("resultObject"));
            }
        } catch (Exception ex) {
            log.warn("Could not fetch agent result object fallback: {}", ex.getMessage());
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractResultObject(PhantomBusterContainerResponse container) {
        if (container.getResultObject() != null) {
            return parseRawResult(container.getResultObject());
        }
        if (container.getData() != null) {
            return container.getData();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseRawResult(Object rawResult) {
        if (rawResult instanceof List<?> list) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    items.add((Map<String, Object>) map);
                }
            }
            return items;
        } else if (rawResult instanceof String jsonStr) {
            try {
                return objectMapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse JSON string resultObject: {}", e.getMessage());
            }
        }
        return Collections.emptyList();
    }
}
