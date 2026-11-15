package com.alumnisearch.service;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.AlumniStatsResponse;
import com.alumnisearch.dto.response.SearchHistoryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AlumniService {

    /**
     * Searches LinkedIn alumni profiles via PhantomBuster API based on university,
     * designation, and optional passout year, persists the retrieved profiles into the database,
     * logs search history, and returns the resulting profiles.
     */
    List<AlumniProfileResponse> searchAndSaveAlumni(AlumniSearchRequest request);

    /**
     * Retrieves all saved alumni profiles with pagination and sorting.
     */
    List<AlumniProfileResponse> getAllSavedAlumni(Pageable pageable);

    /**
     * Retrieves a single alumni profile by its database ID.
     */
    AlumniProfileResponse getAlumniById(Long id);

    /**
     * Dynamic filtering of saved alumni profiles by multiple criteria.
     */
    List<AlumniProfileResponse> filterAlumni(AlumniFilterCriteria criteria, Pageable pageable);

    /**
     * Manually registers a new alumni profile (e.g. added by university or verified alumni).
     */
    AlumniProfileResponse createManualAlumni(AlumniProfileResponse request);

    /**
     * Updates/enriches an existing alumni profile in the database.
     */
    AlumniProfileResponse updateAlumni(Long id, AlumniProfileResponse request);

    /**
     * Updates verification status of an alumni profile (e.g. VERIFIED, PENDING, UNVERIFIED).
     */
    AlumniProfileResponse updateVerificationStatus(Long id, String status);

    /**
     * Calculates aggregated statistical insights from stored alumni profiles.
     */
    AlumniStatsResponse getAlumniStats();

    /**
     * Deletes an alumni profile record by ID.
     */
    void deleteAlumni(Long id);

    /**
     * Exports all stored alumni profiles as a formatted CSV byte array for ATS/HR spreadsheets.
     */
    byte[] exportAlumniCsv();

    /**
     * Bulk imports alumni records from a university CSV spreadsheet file.
     */
    Map<String, Object> importAlumniFromCsv(MultipartFile file);

    /**
     * Generates a sample CSV template for university batch uploads.
     */
    byte[] getCsvTemplate();

    /**
     * Retrieves recent alumni search queries and execution metrics.
     */
    List<SearchHistoryResponse> getSearchHistory();
}
