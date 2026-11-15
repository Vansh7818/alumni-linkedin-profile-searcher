package com.alumnisearch.controller;

import com.alumnisearch.dto.request.UniversityRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.ApiResponse;
import com.alumnisearch.dto.response.UniversityResponse;
import com.alumnisearch.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
@Tag(name = "University & Institution Management", description = "Endpoints for educational institutions to register and manage their alumni networks")
public class UniversityController {

    private final UniversityService universityService;

    @PostMapping
    @Operation(summary = "Register an Educational Institution", description = "Allows universities to create their official institutional profile")
    public ResponseEntity<ApiResponse<UniversityResponse>> registerUniversity(@Valid @RequestBody UniversityRequest request) {
        UniversityResponse response = universityService.registerUniversity(request);
        return ResponseEntity.ok(ApiResponse.success(response, "University registered successfully"));
    }

    @GetMapping
    @Operation(summary = "List All Registered Universities", description = "Retrieves all institutions with computed alumni metrics")
    public ResponseEntity<ApiResponse<List<UniversityResponse>>> getAllUniversities() {
        List<UniversityResponse> universities = universityService.getAllUniversities();
        return ResponseEntity.ok(ApiResponse.success(universities));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get University Details by ID")
    public ResponseEntity<ApiResponse<UniversityResponse>> getUniversityById(@PathVariable Long id) {
        UniversityResponse response = universityService.getUniversityById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{name}/alumni")
    @Operation(summary = "Get Alumni Profiles by University Name")
    public ResponseEntity<ApiResponse<List<AlumniProfileResponse>>> getAlumniByUniversity(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<AlumniProfileResponse> alumni = universityService.getAlumniByUniversity(name, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(alumni));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete University")
    public ResponseEntity<ApiResponse<Void>> deleteUniversity(@PathVariable Long id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.ok(ApiResponse.success(null, "University deleted successfully"));
    }
}
