package com.alumnisearch.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Criteria parameters for querying stored alumni profiles")
public class AlumniFilterCriteria {

    @Schema(description = "Filter by university name (case-insensitive substring)", example = "XYZ")
    private String university;

    @Schema(description = "Filter by designation / current role", example = "Engineer")
    private String designation;

    @Schema(description = "Filter by pass-out year", example = "2020")
    private Integer passoutYear;

    @Schema(description = "Filter by geographic location", example = "New York")
    private String location;

    @Schema(description = "Filter by alumni full name", example = "John")
    private String name;
}
