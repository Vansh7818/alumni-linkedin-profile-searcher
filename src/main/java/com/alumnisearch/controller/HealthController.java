package com.alumnisearch.controller;

import com.alumnisearch.scraper.ScraperStrategyFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health & System Diagnostics", description = "System status and scraper provider availability")
public class HealthController {

    private final ScraperStrategyFactory scraperStrategyFactory;

    @GetMapping
    @Operation(summary = "System Health and Scraper Diagnostic Check")
    public ResponseEntity<Map<String, Object>> getHealth() {
        String activeProvider = scraperStrategyFactory.getPrimaryStrategy().getProviderName();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "alumni-linkedin-searcher",
                "activeScraperProvider", activeProvider,
                "timestamp", LocalDateTime.now()
        ));
    }
}
