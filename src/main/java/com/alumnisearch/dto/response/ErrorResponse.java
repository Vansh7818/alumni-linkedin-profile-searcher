package com.alumnisearch.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response object")
public class ErrorResponse {

    @Schema(description = "Status indicator", example = "error")
    private String status;

    @Schema(description = "Primary error message", example = "Validation failed for search request")
    private String message;

    @Schema(description = "Detailed field validation errors or system diagnostics")
    private List<String> details;

    @Schema(description = "HTTP status code", example = "400")
    private int statusCode;

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "Request path where error occurred", example = "/api/alumni/search")
    private String path;
}
