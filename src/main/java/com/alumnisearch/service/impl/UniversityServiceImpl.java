package com.alumnisearch.service.impl;

import com.alumnisearch.dto.request.UniversityRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.UniversityResponse;
import com.alumnisearch.entity.University;
import com.alumnisearch.exception.BadRequestException;
import com.alumnisearch.exception.ResourceNotFoundException;
import com.alumnisearch.repository.AlumniProfileRepository;
import com.alumnisearch.repository.UniversityRepository;
import com.alumnisearch.service.UniversityService;
import com.alumnisearch.util.AlumniDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final AlumniDataMapper alumniDataMapper;

    @Override
    @Transactional
    public UniversityResponse registerUniversity(UniversityRequest request) {
        String name = request.getName().trim();

        if (universityRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("University with name '" + name + "' already exists");
        }

        University entity = University.builder()
                .name(name)
                .domain(request.getDomain())
                .location(request.getLocation())
                .country(request.getCountry())
                .establishedYear(request.getEstablishedYear())
                .websiteUrl(request.getWebsiteUrl())
                .build();

        University saved = universityRepository.save(entity);
        log.info("Registered new university: {}", name);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UniversityResponse getUniversityById(Long id) {
        University entity = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found with ID: " + id));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileResponse> getAlumniByUniversity(String universityName, Pageable pageable) {
        return alumniProfileRepository.findByUniversityIgnoreCase(universityName)
                .stream()
                .map(alumniDataMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUniversity(Long id) {
        if (!universityRepository.existsById(id)) {
            throw new ResourceNotFoundException("University not found with ID: " + id);
        }
        universityRepository.deleteById(id);
    }

    private UniversityResponse toResponse(University u) {
        long count = alumniProfileRepository.countByUniversityIgnoreCase(u.getName());
        return UniversityResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .domain(u.getDomain())
                .location(u.getLocation())
                .country(u.getCountry())
                .establishedYear(u.getEstablishedYear())
                .websiteUrl(u.getWebsiteUrl())
                .alumniCount(count)
                .createdAt(u.getCreatedAt())
                .build();
    }
}
