package com.alumnisearch.service;

import com.alumnisearch.dto.request.AlumniSearchRequest;
import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.entity.AlumniProfile;
import com.alumnisearch.exception.ExternalServiceException;
import com.alumnisearch.exception.ResourceNotFoundException;
import com.alumnisearch.repository.AlumniProfileRepository;
import com.alumnisearch.scraper.LinkedInScraperStrategy;
import com.alumnisearch.scraper.ScrapedAlumniRecord;
import com.alumnisearch.scraper.ScraperStrategyFactory;
import com.alumnisearch.service.impl.AlumniServiceImpl;
import com.alumnisearch.util.AlumniDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumniServiceTest {

    @Mock
    private AlumniProfileRepository repository;

    @Mock
    private ScraperStrategyFactory scraperStrategyFactory;

    @Mock
    private LinkedInScraperStrategy primaryStrategy;

    @Mock
    private LinkedInScraperStrategy fallbackStrategy;

    @Spy
    private AlumniDataMapper mapper = new AlumniDataMapper();

    @InjectMocks
    private AlumniServiceImpl alumniService;

    private AlumniSearchRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .designation("Software Engineer")
                .passoutYear(2020)
                .build();
    }

    @Test
    @DisplayName("searchAndSaveAlumni should scrape, persist new entities and return responses")
    void searchAndSaveAlumni_newRecords_success() {
        ScrapedAlumniRecord record = ScrapedAlumniRecord.builder()
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinHeadline("Passionate Software Engineer at XYZ Corp")
                .passoutYear(2020)
                .linkedinUrl("https://www.linkedin.com/in/johndoe")
                .company("XYZ Corp")
                .build();

        when(scraperStrategyFactory.getPrimaryStrategy()).thenReturn(primaryStrategy);
        when(primaryStrategy.scrape("University of XYZ", "Software Engineer", 2020))
                .thenReturn(List.of(record));
        when(repository.findByLinkedinUrl("https://www.linkedin.com/in/johndoe")).thenReturn(Optional.empty());
        when(repository.findByNameAndUniversityAndCurrentRole("John Doe", "University of XYZ", "Software Engineer"))
                .thenReturn(Optional.empty());

        when(repository.save(any(AlumniProfile.class))).thenAnswer(invocation -> {
            AlumniProfile p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        List<AlumniProfileResponse> results = alumniService.searchAndSaveAlumni(sampleRequest);

        assertThat(results).hasSize(1);
        AlumniProfileResponse resp = results.get(0);
        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getName()).isEqualTo("John Doe");
        assertThat(resp.getCurrentRole()).isEqualTo("Software Engineer");
        assertThat(resp.getUniversity()).isEqualTo("University of XYZ");
        assertThat(resp.getPassoutYear()).isEqualTo(2020);

        verify(repository, times(1)).save(any(AlumniProfile.class));
    }

    @Test
    @DisplayName("searchAndSaveAlumni should update existing record when profile already exists by LinkedIn URL")
    void searchAndSaveAlumni_existingRecord_updatesInsteadOfDuplicate() {
        ScrapedAlumniRecord record = ScrapedAlumniRecord.builder()
                .name("John Doe")
                .currentRole("Senior Software Engineer")
                .university("University of XYZ")
                .location("Seattle, WA")
                .linkedinHeadline("Staff Engineer at Meta")
                .passoutYear(2020)
                .linkedinUrl("https://www.linkedin.com/in/johndoe")
                .company("Meta")
                .build();

        AlumniProfile existingProfile = AlumniProfile.builder()
                .id(5L)
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinHeadline("Old headline")
                .passoutYear(2020)
                .linkedinUrl("https://www.linkedin.com/in/johndoe")
                .company("XYZ Corp")
                .build();

        when(scraperStrategyFactory.getPrimaryStrategy()).thenReturn(primaryStrategy);
        when(primaryStrategy.scrape("University of XYZ", "Software Engineer", 2020))
                .thenReturn(List.of(record));
        when(repository.findByLinkedinUrl("https://www.linkedin.com/in/johndoe"))
                .thenReturn(Optional.of(existingProfile));

        when(repository.save(any(AlumniProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<AlumniProfileResponse> results = alumniService.searchAndSaveAlumni(sampleRequest);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(5L);
        assertThat(results.get(0).getLocation()).isEqualTo("Seattle, WA");
        assertThat(results.get(0).getLinkedinHeadline()).isEqualTo("Staff Engineer at Meta");

        verify(repository, times(1)).save(existingProfile);
    }

    @Test
    @DisplayName("searchAndSaveAlumni should seamlessly engage fallback scraper if primary fails")
    void searchAndSaveAlumni_fallbackOnPrimaryError() {
        when(scraperStrategyFactory.getPrimaryStrategy()).thenReturn(primaryStrategy);
        when(primaryStrategy.getProviderName()).thenReturn("PhantomBuster");
        when(primaryStrategy.scrape(anyString(), anyString(), any()))
                .thenThrow(new ExternalServiceException("PhantomBuster rate limited"));

        ScrapedAlumniRecord mockRecord = ScrapedAlumniRecord.builder()
                .name("Fallback User")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .passoutYear(2020)
                .build();

        when(scraperStrategyFactory.getFallbackStrategy()).thenReturn(fallbackStrategy);
        when(fallbackStrategy.scrape("University of XYZ", "Software Engineer", 2020))
                .thenReturn(List.of(mockRecord));

        when(repository.save(any(AlumniProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<AlumniProfileResponse> results = alumniService.searchAndSaveAlumni(sampleRequest);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Fallback User");
        verify(fallbackStrategy, times(1)).scrape("University of XYZ", "Software Engineer", 2020);
    }

    @Test
    @DisplayName("getAllSavedAlumni should return paged profile responses")
    void getAllSavedAlumni_success() {
        AlumniProfile p = AlumniProfile.builder()
                .id(1L)
                .name("Jane Smith")
                .currentRole("Data Scientist")
                .university("MIT")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(p)));

        List<AlumniProfileResponse> responses = alumniService.getAllSavedAlumni(pageable);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("getAlumniById should throw ResourceNotFoundException when ID does not exist")
    void getAlumniById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumniService.getAlumniById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteAlumni should delete entity when found")
    void deleteAlumni_success() {
        when(repository.existsById(1L)).thenReturn(true);

        alumniService.deleteAlumni(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}
