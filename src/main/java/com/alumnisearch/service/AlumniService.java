package com.alumnisearch.service;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.AlumniStatsResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AlumniService {

    /**
     * Searches LinkedIn alumni profiles via PhantomBuster API based on university,
     * designation, and optional passout year, persists the retrieved profiles into the database,
     * and returns the resulting profiles.
     *
     * @param request Search criteria payload
     * @return List of persisted alumni profile responses
     */
    List<AlumniProfileResponse> searchAndSaveAlumni(AlumniSearchRequest request);

    /**
     * Retrieves all saved alumni profiles with pagination and sorting.
     *
     * @param pageable Pagination settings
     * @return List of alumni profile responses
     */
    List<AlumniProfileResponse> getAllSavedAlumni(Pageable pageable);

    /**
     * Retrieves a single alumni profile by its database ID.
     *
     * @param id Alumni profile ID
     * @return Alumni profile response
     */
    AlumniProfileResponse getAlumniById(Long id);

    /**
     * Dynamic filtering of saved alumni profiles by multiple criteria.
     *
     * @param criteria Filter criteria
     * @param pageable Pagination settings
     * @return Filtered list of alumni profile responses
     */
    List<AlumniProfileResponse> filterAlumni(AlumniFilterCriteria criteria, Pageable pageable);

    /**
     * Calculates aggregated statistical insights from stored alumni profiles.
     *
     * @return Aggregated statistics
     */
    AlumniStatsResponse getAlumniStats();

    /**
     * Deletes an alumni profile record by ID.
     *
     * @param id Profile ID to delete
     */
    void deleteAlumni(Long id);
}
