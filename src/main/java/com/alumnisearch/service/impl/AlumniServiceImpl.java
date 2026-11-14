package com.alumnisearch.service.impl;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.AlumniStatsResponse;
import com.alumnisearch.entity.AlumniProfile;
import com.alumnisearch.exception.ExternalServiceException;
import com.alumnisearch.exception.ResourceNotFoundException;
import com.alumnisearch.repository.AlumniProfileRepository;
import com.alumnisearch.repository.AlumniSpecification;
import com.alumnisearch.scraper.LinkedInScraperStrategy;
import com.alumnisearch.scraper.ScrapedAlumniRecord;
import com.alumnisearch.scraper.ScraperStrategyFactory;
import com.alumnisearch.service.AlumniService;
import com.alumnisearch.util.AlumniDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlumniServiceImpl implements AlumniService {

    private final AlumniProfileRepository repository;
    private final ScraperStrategyFactory scraperStrategyFactory;
    private final AlumniDataMapper mapper;

    @Override
    @Transactional
    public List<AlumniProfileResponse> searchAndSaveAlumni(AlumniSearchRequest request) {
        String university = request.getUniversity().trim();
        String designation = request.getDesignation().trim();
        Integer passoutYear = request.getPassoutYear();

        log.info("Executing alumni search and persistence for University: '{}', Role: '{}', Year: '{}'",
                university, designation, passoutYear);

        LinkedInScraperStrategy strategy = scraperStrategyFactory.getPrimaryStrategy();
        List<ScrapedAlumniRecord> scrapedRecords;

        try {
            scrapedRecords = strategy.scrape(university, designation, passoutYear);
        } catch (ExternalServiceException ex) {
            log.warn("Primary scraper strategy '{}' encountered error: {}. Engaging fallback scraper.",
                    strategy.getProviderName(), ex.getMessage());
            scrapedRecords = scraperStrategyFactory.getFallbackStrategy().scrape(university, designation, passoutYear);
        }

        String searchQuery = university + " | " + designation + (passoutYear != null ? " | " + passoutYear : "");
        List<AlumniProfile> persistedEntities = new ArrayList<>();

        for (ScrapedAlumniRecord record : scrapedRecords) {
            AlumniProfile entity = findExistingOrNew(record, university, designation, searchQuery);
            AlumniProfile saved = repository.save(entity);
            persistedEntities.add(saved);
        }

        log.info("Successfully persisted {} alumni profiles in database", persistedEntities.size());

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
    public void deleteAlumni(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Alumni profile not found with ID: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted alumni profile with ID: {}", id);
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
}
