package com.alumnisearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "phantombuster")
public class PhantomBusterProperties {

    /**
     * PhantomBuster API Key (found in workspace settings).
     */
    private String apiKey;

    /**
     * Agent ID (Phantom) configured for LinkedIn search or scraping.
     */
    private String agentId;

    /**
     * Base URL for PhantomBuster REST API v2.
     */
    private String baseUrl = "https://api.phantombuster.com/api/v2";

    /**
     * Polling interval in milliseconds when waiting for agent container completion.
     */
    private long pollIntervalMs = 3000;

    /**
     * Maximum wait time in seconds before timing out an agent run.
     */
    private int maxWaitSeconds = 120;

    /**
     * Configuration for Mock mode.
     */
    private MockMode mockMode = new MockMode();

    @Data
    public static class MockMode {
        /**
         * When true, uses simulated high-fidelity scraping without calling PhantomBuster.
         * Also automatically used if apiKey is missing or blank.
         */
        private boolean enabled = false;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("OPTIONAL");
    }
}
