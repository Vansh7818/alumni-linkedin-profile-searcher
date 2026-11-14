package com.alumnisearch.scraper;

import com.alumnisearch.config.PhantomBusterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperStrategyFactory {

    private final PhantomBusterScraperService phantomBusterScraperService;
    private final MockLinkedInScraperService mockLinkedInScraperService;
    private final PhantomBusterProperties properties;

    /**
     * Resolves the primary scraper strategy based on current configuration.
     * Defaults to Mock mode if PhantomBuster API key is unset or mock-mode is enabled.
     */
    public LinkedInScraperStrategy getPrimaryStrategy() {
        if (properties.getMockMode().isEnabled()) {
            log.info("PhantomBuster mock mode is explicitly enabled. Using MockLinkedInScraperService.");
            return mockLinkedInScraperService;
        }

        if (phantomBusterScraperService.isAvailable()) {
            log.info("PhantomBuster API credentials detected. Using PhantomBusterScraperService.");
            return phantomBusterScraperService;
        }

        log.info("PhantomBuster credentials not fully configured. Seamlessly falling back to MockLinkedInScraperService.");
        return mockLinkedInScraperService;
    }

    public LinkedInScraperStrategy getFallbackStrategy() {
        return mockLinkedInScraperService;
    }
}
