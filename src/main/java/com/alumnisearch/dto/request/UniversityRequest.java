package com.alumnisearch.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create or register an educational institution")
public class UniversityRequest {

    @NotBlank(message = "University name is required")
    @Schema(description = "Full name of the university / institution", example = "Stanford University")
    private String name;

    @Schema(description = "Official domain name", example = "stanford.edu")
    private String domain;

    @Schema(description = "City and State", example = "Stanford, CA")
    private String location;

    @Schema(description = "Country", example = "United States")
    private String country;

    @Schema(description = "Year founded", example = "1885")
    private Integer establishedYear;

    @Schema(description = "Official website URL", example = "https://www.stanford.edu")
    private String websiteUrl;
}
