package com.alumnisearch.scraper;

import com.alumnisearch.config.PhantomBusterProperties;
import com.alumnisearch.exception.ExternalServiceException;
import com.alumnisearch.util.AlumniDataMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PhantomBusterScraperServiceTest {

    @Mock
    private RestClient restClient;

    @Spy
    private PhantomBusterProperties properties = new PhantomBusterProperties();

    @Spy
    private AlumniDataMapper mapper = new AlumniDataMapper();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PhantomBusterScraperService service;

    @Test
    @DisplayName("isAvailable should return false when apiKey is blank")
    void isAvailable_withoutKey_returnsFalse() {
        properties.setApiKey("");
        assertThat(service.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("isAvailable should return false when mock mode is enabled")
    void isAvailable_withMockMode_returnsFalse() {
        properties.setApiKey("valid-key");
        properties.getMockMode().setEnabled(true);
        assertThat(service.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("isAvailable should return true when apiKey is present and mock mode is false")
    void isAvailable_withValidKey_returnsTrue() {
        properties.setApiKey("valid-key");
        properties.getMockMode().setEnabled(false);
        assertThat(service.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("scrape should throw ExternalServiceException if called when unavailable")
    void scrape_whenUnavailable_throwsException() {
        properties.setApiKey("");
        assertThatThrownBy(() -> service.scrape("Harvard", "CTO", 2018))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    @DisplayName("getProviderName should return PhantomBuster")
    void getProviderName_returnsExpected() {
        assertThat(service.getProviderName()).isEqualTo("PhantomBuster");
    }
}
