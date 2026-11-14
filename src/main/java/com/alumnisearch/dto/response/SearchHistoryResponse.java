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
@Schema(description = "Record of historical alumni searches")
public class SearchHistoryResponse {

    private Long id;
    private String university;
    private String designation;
    private Integer passoutYear;
    private int resultsCount;
    private long executionTimeMs;
    private String provider;
    private LocalDateTime searchedAt;
}
