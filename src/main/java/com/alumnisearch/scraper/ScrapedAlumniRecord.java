package com.alumnisearch.scraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedAlumniRecord {

    private String name;
    private String currentRole;
    private String university;
    private String location;
    private String linkedinHeadline;
    private Integer passoutYear;
    private String linkedinUrl;
    private String company;
}
