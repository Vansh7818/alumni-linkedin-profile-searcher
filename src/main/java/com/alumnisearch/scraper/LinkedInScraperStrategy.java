package com.alumnisearch.scraper;

import java.util.List;

public interface LinkedInScraperStrategy {

    /**
     * Executes LinkedIn alumni discovery for given university and designation.
     *
     * @param university  University or institution name
     * @param designation Current role or title
     * @param passoutYear Optional passout / graduation year
     * @return List of discovered alumni records
     */
    List<ScrapedAlumniRecord> scrape(String university, String designation, Integer passoutYear);

    /**
     * Provider identification name (e.g., "PhantomBuster", "Mock").
     */
    String getProviderName();

    /**
     * Checks if this scraper strategy is active and ready.
     */
    boolean isAvailable();
}
