package com.alumnisearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated statistical insights across stored alumni records")
public class AlumniStatsResponse {

    @Schema(description = "Total number of alumni profiles stored in database", example = "42")
    private long totalProfiles;

    @Schema(description = "Count of alumni grouped by university")
    private Map<String, Long> topUniversities;

    @Schema(description = "Count of alumni grouped by designation")
    private Map<String, Long> topRoles;

    @Schema(description = "Count of alumni grouped by graduation year")
    private Map<Integer, Long> graduationYearDistribution;
}
