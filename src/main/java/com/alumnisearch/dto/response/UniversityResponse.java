package com.alumnisearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "University record with associated alumni metrics")
public class UniversityResponse {

    private Long id;
    private String name;
    private String domain;
    private String location;
    private String country;
    private Integer establishedYear;
    private String websiteUrl;
    private long alumniCount;
    private LocalDateTime createdAt;
}
