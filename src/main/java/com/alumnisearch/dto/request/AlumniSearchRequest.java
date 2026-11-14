package com.alumnisearch.dto.request;

import com.alumnisearch.util.PassoutYearDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
@Schema(description = "Payload to search alumni LinkedIn profiles via university, designation, and optional graduation year")
public class AlumniSearchRequest {

    @NotBlank(message = "University name is mandatory and cannot be blank")
    @Schema(description = "Name of the educational institution / university", example = "University of XYZ")
    private String university;

    @NotBlank(message = "Designation is mandatory and cannot be blank")
    @Schema(description = "Current job title or designation of the alumni", example = "Software Engineer")
    private String designation;

    @JsonDeserialize(using = PassoutYearDeserializer.class)
    @Schema(description = "Optional year of graduation / passout", example = "2020", nullable = true)
    private Integer passoutYear;
}
