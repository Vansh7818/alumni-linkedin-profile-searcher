package com.alumnisearch.scraper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockLinkedInScraperServiceTest {

    private final MockLinkedInScraperService service = new MockLinkedInScraperService();

    @Test
    @DisplayName("Mock scraper should generate non-empty list of alumni records matching university and role")
    void scrape_generatesRealisticAlumni() {
        List<ScrapedAlumniRecord> results = service.scrape("University of XYZ", "Software Engineer", 2020);

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isBetween(3, 7);

        for (ScrapedAlumniRecord record : results) {
            assertThat(record.getName()).isNotBlank();
            assertThat(record.getCurrentRole()).isEqualTo("Software Engineer");
            assertThat(record.getUniversity()).isEqualTo("University of XYZ");
            assertThat(record.getLocation()).isNotBlank();
            assertThat(record.getLinkedinHeadline()).contains("Software Engineer");
            assertThat(record.getLinkedinUrl()).startsWith("https://www.linkedin.com/in/");
            assertThat(record.getPassoutYear()).isEqualTo(2020);
        }
    }

    @Test
    @DisplayName("Mock scraper should handle null passout year by generating valid years")
    void scrape_withoutPassoutYear() {
        List<ScrapedAlumniRecord> results = service.scrape("Stanford", "Data Scientist", null);

        assertThat(results).isNotEmpty();
        for (ScrapedAlumniRecord record : results) {
            assertThat(record.getCurrentRole()).isEqualTo("Data Scientist");
            assertThat(record.getPassoutYear()).isNotNull();
        }
    }
}
