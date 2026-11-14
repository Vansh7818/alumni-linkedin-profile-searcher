package com.alumnisearch.controller;

import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.dto.response.AlumniStatsResponse;
import com.alumnisearch.exception.ResourceNotFoundException;
import com.alumnisearch.service.AlumniService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlumniSearchController.class)
class AlumniSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlumniService alumniService;

    @Test
    @DisplayName("POST /api/alumni/search should return 200 and expected alumni JSON response")
    void searchAlumni_success() throws Exception {
        AlumniProfileResponse profile1 = AlumniProfileResponse.builder()
                .id(1L)
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinHeadline("Passionate Software Engineer at XYZ Corp")
                .passoutYear(2020)
                .build();

        AlumniProfileResponse profile2 = AlumniProfileResponse.builder()
                .id(2L)
                .name("Jane Smith")
                .currentRole("Data Scientist")
                .university("University of XYZ")
                .location("San Francisco, CA")
                .linkedinHeadline("Data Scientist | AI Enthusiast")
                .passoutYear(2019)
                .build();

        when(alumniService.searchAndSaveAlumni(any(AlumniSearchRequest.class)))
                .thenReturn(List.of(profile1, profile2));

        String requestBody = """
                {
                  "university": "University of XYZ",
                  "designation": "Software Engineer",
                  "passoutYear": 2020
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(jsonPath("$.data[0].currentRole").value("Software Engineer"))
                .andExpect(jsonPath("$.data[0].university").value("University of XYZ"))
                .andExpect(jsonPath("$.data[0].location").value("New York, NY"))
                .andExpect(jsonPath("$.data[0].linkedinHeadline").value("Passionate Software Engineer at XYZ Corp"))
                .andExpect(jsonPath("$.data[0].passoutYear").value(2020))
                .andExpect(jsonPath("$.data[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$.data[1].currentRole").value("Data Scientist"))
                .andExpect(jsonPath("$.data[1].passoutYear").value(2019));
    }

    @Test
    @DisplayName("POST /api/alumni/search should handle string passoutYear 'OPTIONAL_PASSOUT_YEAR' gracefully")
    void searchAlumni_withStringPassoutYear_success() throws Exception {
        AlumniProfileResponse profile = AlumniProfileResponse.builder()
                .name("Carlos Gomez")
                .currentRole("Backend Engineer")
                .university("Stanford University")
                .passoutYear(2021)
                .build();

        when(alumniService.searchAndSaveAlumni(any(AlumniSearchRequest.class)))
                .thenReturn(List.of(profile));

        String requestBody = """
                {
                  "university": "Stanford University",
                  "designation": "Backend Engineer",
                  "passoutYear": "OPTIONAL_PASSOUT_YEAR"
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("Carlos Gomez"));
    }

    @Test
    @DisplayName("POST /api/alumni/search should return 400 when university is missing")
    void searchAlumni_missingUniversity_shouldReturn400() throws Exception {
        String requestBody = """
                {
                  "university": "",
                  "designation": "Software Engineer"
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.details", hasItem(containsString("university"))));
    }

    @Test
    @DisplayName("POST /api/alumni/search should return 400 when designation is missing")
    void searchAlumni_missingDesignation_shouldReturn400() throws Exception {
        String requestBody = """
                {
                  "university": "MIT",
                  "designation": "   "
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.details", hasItem(containsString("designation"))));
    }

    @Test
    @DisplayName("GET /api/alumni/all should return 200 with list of saved profiles")
    void getAllSavedAlumni_success() throws Exception {
        AlumniProfileResponse profile = AlumniProfileResponse.builder()
                .id(1L)
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .build();

        when(alumniService.getAllSavedAlumni(any(Pageable.class)))
                .thenReturn(List.of(profile));

        mockMvc.perform(get("/api/alumni/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/alumni/{id} should return 404 when profile does not exist")
    void getAlumniById_notFound() throws Exception {
        when(alumniService.getAlumniById(999L))
                .thenThrow(new ResourceNotFoundException("Alumni profile not found with ID: 999"));

        mockMvc.perform(get("/api/alumni/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(containsString("not found with ID: 999")));
    }

    @Test
    @DisplayName("GET /api/alumni/stats should return aggregated statistics")
    void getStats_success() throws Exception {
        AlumniStatsResponse stats = AlumniStatsResponse.builder()
                .totalProfiles(25L)
                .topUniversities(Map.of("University of XYZ", 15L))
                .topRoles(Map.of("Software Engineer", 10L))
                .build();

        when(alumniService.getAlumniStats()).thenReturn(stats);

        mockMvc.perform(get("/api/alumni/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalProfiles").value(25))
                .andExpect(jsonPath("$.data.topUniversities['University of XYZ']").value(15));
    }
}
