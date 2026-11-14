package com.alumnisearch.controller;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.AlumniStatsResponse;
import com.alumnisearch.dto.response.ApiResponse;
import com.alumnisearch.dto.response.SearchHistoryResponse;
import com.alumnisearch.service.AlumniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@Tag(name = "Alumni Profile Searcher", description = "Operations for searching, persisting, exporting, and querying LinkedIn alumni profiles")
public class AlumniSearchController {

    private final AlumniService alumniService;

    @PostMapping("/search")
    @Operation(
            summary = "Search Alumni Profiles via LinkedIn / PhantomBuster API",
            description = "Accepts university name, current designation, and an optional pass-out year. " +
                    "Triggers LinkedIn scraping via PhantomBuster API, persists retrieved profiles into the database, " +
                    "and returns the saved alumni list."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Alumni profiles searched and persisted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": [
                                                {
                                                  "name": "John Doe",
                                                  "currentRole": "Software Engineer",
                                                  "university": "University of XYZ",
                                                  "location": "New York, NY",
                                                  "linkedinHeadline": "Passionate Software Engineer at XYZ Corp",
                                                  "passoutYear": 2020
                                                },
                                                {
                                                  "name": "Jane Smith",
                                                  "currentRole": "Data Scientist",
                                                  "university": "University of XYZ",
                                                  "location": "San Francisco, CA",
                                                  "linkedinHeadline": "Data Scientist | AI Enthusiast",
                                                  "passoutYear": 2019
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "PhantomBuster API communication failure")
    })
    public ResponseEntity<ApiResponse<List<AlumniProfileResponse>>> searchAlumni(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Search parameters specifying target educational institution, designation, and optional year",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AlumniSearchRequest.class),
                            examples = @ExampleObject(
                                    name = "SampleSearchRequest",
                                    value = """
                                            {
                                              "university": "University of XYZ",
                                              "designation": "Software Engineer",
                                              "passoutYear": 2020
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody AlumniSearchRequest request) {

        log.info("Received search request: university='{}', designation='{}', passoutYear={}",
                request.getUniversity(), request.getDesignation(), request.getPassoutYear());

        List<AlumniProfileResponse> profiles = alumniService.searchAndSaveAlumni(request);
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get All Saved Alumni Profiles",
            description = "Retrieves all alumni profiles stored in the database. Supports pagination and sorting."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of saved alumni profiles retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<AlumniProfileResponse>>> getAllSavedAlumni(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "50")
            @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort property", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        List<AlumniProfileResponse> profiles = alumniService.getAllSavedAlumni(pageable);
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Saved Alumni Profile by ID", description = "Retrieves a specific alumni profile by its unique database ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alumni profile found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ApiResponse<AlumniProfileResponse>> getAlumniById(
            @Parameter(description = "Alumni database ID", example = "1")
            @PathVariable Long id) {

        AlumniProfileResponse profile = alumniService.getAlumniById(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update / Enrich Alumni Profile", description = "Updates details of a saved alumni profile")
    public ResponseEntity<ApiResponse<AlumniProfileResponse>> updateAlumni(
            @PathVariable Long id,
            @RequestBody AlumniProfileResponse updateRequest) {

        AlumniProfileResponse updated = alumniService.updateAlumni(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter Saved Alumni Profiles", description = "Dynamic multi-criteria search over saved alumni records")
    public ResponseEntity<ApiResponse<List<AlumniProfileResponse>>> filterAlumni(
            @Parameter(description = "University name filter") @RequestParam(required = false) String university,
            @Parameter(description = "Designation filter") @RequestParam(required = false) String designation,
            @Parameter(description = "Passout year filter") @RequestParam(required = false) Integer passoutYear,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Alumni name filter") @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        AlumniFilterCriteria criteria = AlumniFilterCriteria.builder()
                .university(university)
                .designation(designation)
                .passoutYear(passoutYear)
                .location(location)
                .name(name)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AlumniProfileResponse> results = alumniService.filterAlumni(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/stats")
    @Operation(summary = "Alumni Aggregation Statistics", description = "Returns summary analytics: total alumni, top universities, roles, and graduation years")
    public ResponseEntity<ApiResponse<AlumniStatsResponse>> getStats() {
        AlumniStatsResponse stats = alumniService.getAlumniStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/history")
    @Operation(summary = "Recent Search Audit History", description = "Returns the 20 most recent alumni searches and execution durations")
    public ResponseEntity<ApiResponse<List<SearchHistoryResponse>>> getSearchHistory() {
        List<SearchHistoryResponse> history = alumniService.getSearchHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export Alumni to CSV", description = "Streams all persisted alumni records as a CSV file for ATS/Spreadsheet analysis")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csvData = alumniService.exportAlumniCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"alumni_profiles.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Saved Alumni Profile", description = "Deletes an alumni record by its database ID")
    public ResponseEntity<ApiResponse<Void>> deleteAlumni(@PathVariable Long id) {
        alumniService.deleteAlumni(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Alumni profile " + id + " deleted successfully"));
    }

    @PostMapping("/webhook")
    @Operation(summary = "PhantomBuster Webhook Handler", description = "Receives asynchronous execution callbacks when a Phantom completes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleWebhook(@RequestBody(required = false) Map<String, Object> payload) {
        log.info("Received PhantomBuster webhook payload: {}", payload);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "received"), "Webhook processed"));
    }
}
