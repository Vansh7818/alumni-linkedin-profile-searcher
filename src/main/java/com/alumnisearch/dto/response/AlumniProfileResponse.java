package com.alumnisearch.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Alumni LinkedIn profile representation")
public class AlumniProfileResponse {

    @Schema(description = "Internal unique identifier", example = "1")
    private Long id;

    @Schema(description = "Full name of the alumni", example = "John Doe")
    private String name;

    @Schema(description = "Current designation or professional role", example = "Software Engineer")
    private String currentRole;

    @Schema(description = "Name of the university / educational institution", example = "University of XYZ")
    private String university;

    @Schema(description = "Geographic location", example = "New York, NY")
    private String location;

    @Schema(description = "LinkedIn headline summary", example = "Passionate Software Engineer at XYZ Corp")
    private String linkedinHeadline;

    @Schema(description = "Graduation / passout year", example = "2020")
    private Integer passoutYear;

    @Schema(description = "Public LinkedIn profile URL", example = "https://www.linkedin.com/in/johndoe")
    private String linkedinUrl;

    @Schema(description = "Current employer or organization", example = "XYZ Corp")
    private String company;

    @Schema(description = "Alumni email address", example = "john.doe@alumni.xyz.edu")
    private String email;

    @Schema(description = "Key technical and professional skills", example = "Java, Spring Boot, PostgreSQL, Microservices")
    private String skills;

    @Schema(description = "Verification status", example = "VERIFIED")
    private String verificationStatus;
}
