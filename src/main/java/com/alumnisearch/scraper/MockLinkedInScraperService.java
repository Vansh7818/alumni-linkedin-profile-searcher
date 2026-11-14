package com.alumnisearch.scraper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class MockLinkedInScraperService implements LinkedInScraperStrategy {

    private final Random random = new Random();

    private static final String[][] PROFILE_SEEDS = {
            {"John Doe", "New York, NY", "Passionate %s at XYZ Corp", "XYZ Corp"},
            {"Jane Smith", "San Francisco, CA", "%s | AI & Cloud Enthusiast", "Google"},
            {"Alex Johnson", "Seattle, WA", "Experienced %s | Ex-Amazon", "Amazon"},
            {"Priya Sharma", "Bengaluru, India", "%s @ Microsoft | Tech Speaker", "Microsoft"},
            {"Carlos Mendoza", "Austin, TX", "%s | Distributed Systems & High-Scale Infra", "Meta"},
            {"Aisha Al-Mansoor", "London, UK", "%s & Engineering Lead", "Bloomberg"},
            {"Michael Chen", "Boston, MA", "%s | Open Source Contributor", "Stripe"},
            {"Sophia Williams", "Chicago, IL", "Senior %s driving cloud innovation", "Uber"}
    };

    @Override
    public String getProviderName() {
        return "MockScraper";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<ScrapedAlumniRecord> scrape(String university, String designation, Integer passoutYear) {
        log.info("Generating realistic mock alumni records for University: '{}', Role: '{}', Year: '{}'",
                university, designation, passoutYear);

        List<ScrapedAlumniRecord> results = new ArrayList<>();
        int count = 3 + random.nextInt(4); // 3 to 6 realistic profiles

        int effectiveYear = (passoutYear != null) ? passoutYear : (2018 + random.nextInt(6));

        for (int i = 0; i < count; i++) {
            String[] seed = PROFILE_SEEDS[i % PROFILE_SEEDS.length];
            String name = seed[0];
            String location = seed[1];
            String headline = String.format(seed[2], designation);
            String company = seed[3];
            int profileYear = (passoutYear != null) ? passoutYear : (effectiveYear - (i % 4));

            String slug = name.toLowerCase().replace(" ", "-") + "-" + (100 + i);
            String linkedinUrl = "https://www.linkedin.com/in/" + slug;

            ScrapedAlumniRecord record = ScrapedAlumniRecord.builder()
                    .name(name)
                    .currentRole(designation)
                    .university(university)
                    .location(location)
                    .linkedinHeadline(headline)
                    .passoutYear(profileYear)
                    .linkedinUrl(linkedinUrl)
                    .company(company)
                    .build();

            results.add(record);
        }

        return results;
    }
}
