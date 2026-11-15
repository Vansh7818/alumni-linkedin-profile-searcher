package com.alumnisearch.service;

import com.alumnisearch.dto.request.UniversityRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.UniversityResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UniversityService {

    UniversityResponse registerUniversity(UniversityRequest request);

    List<UniversityResponse> getAllUniversities();

    UniversityResponse getUniversityById(Long id);

    List<AlumniProfileResponse> getAlumniByUniversity(String universityName, Pageable pageable);

    void deleteUniversity(Long id);
}
