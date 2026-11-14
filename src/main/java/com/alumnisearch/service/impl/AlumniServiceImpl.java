package com.alumnisearch.service.impl;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.AlumniStatsResponse;
import com.alumnisearch.dto.response.SearchHistoryResponse;
import com.alumnisearch.entity.AlumniProfile;
import com.alumnisearch.entity.SearchHistory;
import com.alumnisearch.exception.ExternalServiceException;
import com.alumnisearch.exception.ResourceNotFoundException;
import com.alumnisearch.repository.AlumniProfileRepository;
import com.alumnisearch.repository.AlumniSpecification;
import com.alumnisearch.repository.SearchHistoryRepository;
import com.alumnisearch.scraper.LinkedInScraperStrategy;
import com.alumnisearch.scraper.ScrapedAlumniRecord;
import com.alumnisearch.scraper.ScraperStrategyFactory;
import com.alumnisearch.service.AlumniService;
import com.alumnisearch.util.AlumniDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlumniServiceImpl implements AlumniService {

    private final AlumniProfileRepository repository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final ScraperStrategyFactory scraperStrategyFactory;
    private final AlumniDataMapper mapper;

    @Override
    @Transactional
    @Cacheable(value = "alumniSearches", key = "#request.university + '_' + #request.designation + '_' + #request.passoutYear")
    public List<AlumniProfileResponse> searchAndSaveAlumni(AlumniSearchRequest request) {
        long startTime = System.currentTimeMillis();
        String university = request.getUniversity().trim();
        String designation = request.getDesignation().trim();
        Integer passoutYear = request.getPassoutYear();

        log.info("Executing alumni search and persistence for University: '{}', Role: '{}', Year: '{}'",
                university, designation, passoutYear);

        LinkedInScraperStrategy strategy = scraperStrategyFactory.getPrimaryStrategy();
        List<ScrapedAlumniRecord> scrapedRecords;
        String providerUsed = strategy.getProviderName();

        try {
            scrapedRecords = strategy.scrape(university, designation, passoutYear);
        } catch (ExternalServiceException ex) {
            log.warn("Primary scraper strategy '{}' encountered error: {}. Engaging fallback scraper.",
                    strategy.getProviderName(), ex.getMessage());
            strategy = scraperStrategyFactory.getFallbackStrategy();
            providerUsed = strategy.getProviderName() + " (Fallback)";
            scrapedRecords = strategy.scrape(university, designation, passoutYear);
        }

        String searchQuery = university + " | " + designation + (passoutYear != null ? " | " + passoutYear : "");
        List<AlumniProfile> persistedEntities = new ArrayList<>();

        for (ScrapedAlumniRecord record : scrapedRecords) {
            AlumniProfile entity = findExistingOrNew(record, university, designation, searchQuery);
            AlumniProfile saved = repository.save(entity);
            persistedEntities.add(saved);
        }

        long executionTime = System.currentTimeMillis() - startTime;

        // Record Search History for Audit & Analytics
        try {
            SearchHistory history = SearchHistory.builder()
                    .university(university)
                    .designation(designation)
                    .passoutYear(passoutYear)
                    .resultsCount(persistedEntities.size())
                    .executionTimeMs(executionTime)
                    .provider(providerUsed)
                    .build();
            searchHistoryRepository.save(history);
        } catch (Exception ex) {
            log.warn("Could not save search history: {}", ex.getMessage());
        }

        log.info("Successfully persisted {} alumni profiles in database in {}ms", persistedEntities.size(), executionTime);

        return persistedEntities.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getAllSavedAlumni(Pageable pageable) {
        return repository.findAll(pageable)
                .getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniProfileResponse getAlumniById(Long id) {
        AlumniProfile entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni profile not found with ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> filterAlumni(AlumniFilterCriteria criteria, Pageable pageable) {
        return repository.findAll(AlumniSpecification.withCriteria(criteria), pageable)
                .getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "alumniSearches", allEntries = true)
    public AlumniProfileResponse updateAlumni(Long id, AlumniProfileResponse updateRequest) {
        AlumniProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni profile not found with ID: " + id));

        if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
            profile.setName(updateRequest.getName().trim());
        }
        if (updateRequest.getCurrentRole() != null && !updateRequest.getCurrentRole().isBlank()) {
            profile.setCurrentRole(updateRequest.getCurrentRole().trim());
        }
        if (updateRequest.getUniversity() != null && !updateRequest.getUniversity().isBlank()) {
            profile.setUniversity(updateRequest.getUniversity().trim());
        }
        if (updateRequest.getLocation() != null) {
            profile.setLocation(updateRequest.getLocation().trim());
        }
        if (updateRequest.getLinkedinHeadline() != null) {
            profile.setLinkedinHeadline(updateRequest.getLinkedinHeadline().trim());
        }
        if (updateRequest.getPassoutYear() != null) {
            profile.setPassoutYear(updateRequest.getPassoutYear());
        }
        if (updateRequest.getCompany() != null) {
            profile.setCompany(updateRequest.getCompany().trim());
        }
        if (updateRequest.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(updateRequest.getLinkedinUrl().trim());
        }

        AlumniProfile saved = repository.save(profile);
        log.info("Updated alumni profile with ID: {}", id);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AlumniStatsResponse getAlumniStats() {
        long total = repository.count();

        Map<String, Long> topUniversities = new LinkedHashMap<>();
        for (Object[] row : repository.countAlumniByUniversity()) {
            topUniversities.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Long> topRoles = new LinkedHashMap<>();
        for (Object[] row : repository.countAlumniByRole()) {
            topRoles.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<Integer, Long> yearDist = new LinkedHashMap<>();
        for (Object[] row : repository.countAlumniByPassoutYear()) {
            if (row[0] != null) {
                yearDist.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            }
        }

        return AlumniStatsResponse.builder()
                .totalProfiles(total)
                .topUniversities(topUniversities)
                .topRoles(topRoles)
                .graduationYearDistribution(yearDist)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "alumniSearches", allEntries = true)
    public void deleteAlumni(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Alumni profile not found with ID: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted alumni profile with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAlumniCsv() {
        List<AlumniProfile> profiles = repository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Current Role,University,Location,Passout Year,Company,LinkedIn Headline,LinkedIn URL\n");

        for (AlumniProfile p : profiles) {
            csv.append(escapeCsv(p.getId() != null ? p.getId().toString() : "")).append(",")
                    .append(escapeCsv(p.getName())).append(",")
                    .append(escapeCsv(p.getCurrentRole())).append(",")
                    .append(escapeCsv(p.getUniversity())).append(",")
                    .append(escapeCsv(p.getLocation())).append(",")
                    .append(escapeCsv(p.getPassoutYear() != null ? p.getPassoutYear().toString() : "")).append(",")
                    .append(escapeCsv(p.getCompany())).append(",")
                    .append(escapeCsv(p.getLinkedinHeadline())).append(",")
                    .append(escapeCsv(p.getLinkedinUrl())).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> getSearchHistory() {
        return searchHistoryRepository.findTop20ByOrderBySearchedAtDesc()
                .stream()
                .map(h -> SearchHistoryResponse.builder()
                        .id(h.getId())
                        .university(h.getUniversity())
                        .designation(h.getDesignation())
                        .passoutYear(h.getPassoutYear())
                        .resultsCount(h.getResultsCount())
                        .executionTimeMs(h.getExecutionTimeMs())
                        .provider(h.getProvider())
                        .searchedAt(h.getSearchedAt())
                        .build())
                .toList();
    }

    private AlumniProfile findExistingOrNew(ScrapedAlumniRecord record, String university, String designation, String searchQuery) {
        Optional<AlumniProfile> existing = Optional.empty();

        if (record.getLinkedinUrl() != null && !record.getLinkedinUrl().isBlank()) {
            existing = repository.findByLinkedinUrl(record.getLinkedinUrl().trim());
        }

        if (existing.isEmpty() && record.getName() != null) {
            existing = repository.findByNameAndUniversityAndCurrentRole(
                    record.getName().trim(),
                    record.getUniversity() != null ? record.getUniversity().trim() : university,
                    record.getCurrentRole() != null ? record.getCurrentRole().trim() : designation
            );
        }

        if (existing.isPresent()) {
            AlumniProfile profile = existing.get();
            // Update fields with latest scraped data
            if (record.getLocation() != null) profile.setLocation(record.getLocation());
            if (record.getLinkedinHeadline() != null) profile.setLinkedinHeadline(record.getLinkedinHeadline());
            if (record.getPassoutYear() != null) profile.setPassoutYear(record.getPassoutYear());
            if (record.getCompany() != null) profile.setCompany(record.getCompany());
            profile.setSearchQuery(searchQuery);
            return profile;
        }

        return mapper.toEntity(record, university, designation, searchQuery);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
