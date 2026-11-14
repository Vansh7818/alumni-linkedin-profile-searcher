package com.alumnisearch.repository;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.entity.AlumniProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AlumniProfileRepositoryTest {

    @Autowired
    private AlumniProfileRepository repository;

    @Test
    @DisplayName("Saving and querying alumni profile by LinkedIn URL")
    void saveAndFindByLinkedinUrl() {
        AlumniProfile profile = AlumniProfile.builder()
                .name("Alice Wonder")
                .currentRole("DevOps Engineer")
                .university("University of Oxford")
                .location("London, UK")
                .linkedinHeadline("DevOps Engineer at CloudCo")
                .passoutYear(2018)
                .linkedinUrl("https://www.linkedin.com/in/alicewonder")
                .build();

        repository.save(profile);

        Optional<AlumniProfile> found = repository.findByLinkedinUrl("https://www.linkedin.com/in/alicewonder");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Wonder");
        assertThat(found.get().getPassoutYear()).isEqualTo(2018);
    }

    @Test
    @DisplayName("Filtering alumni using dynamic Criteria specification")
    void filterWithSpecification() {
        AlumniProfile p1 = AlumniProfile.builder()
                .name("Bob Builder")
                .currentRole("Cloud Architect")
                .university("Carnegie Mellon University")
                .location("Pittsburgh, PA")
                .passoutYear(2017)
                .build();

        AlumniProfile p2 = AlumniProfile.builder()
                .name("Charlie Brown")
                .currentRole("Frontend Engineer")
                .university("UC Berkeley")
                .location("San Francisco, CA")
                .passoutYear(2022)
                .build();

        repository.saveAll(List.of(p1, p2));

        AlumniFilterCriteria criteria = AlumniFilterCriteria.builder()
                .university("Berkeley")
                .designation("Frontend")
                .build();

        Page<AlumniProfile> result = repository.findAll(AlumniSpecification.withCriteria(criteria), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Charlie Brown");
    }

    @Test
    @DisplayName("Aggregated statistics queries")
    void countAlumniAggregations() {
        AlumniProfile p1 = AlumniProfile.builder()
                .name("User 1")
                .currentRole("Software Engineer")
                .university("MIT")
                .passoutYear(2020)
                .build();

        AlumniProfile p2 = AlumniProfile.builder()
                .name("User 2")
                .currentRole("Software Engineer")
                .university("MIT")
                .passoutYear(2020)
                .build();

        repository.saveAll(List.of(p1, p2));

        List<Object[]> uniCount = repository.countAlumniByUniversity();
        assertThat(uniCount).isNotEmpty();
        assertThat((String) uniCount.get(0)[0]).isEqualTo("MIT");
        assertThat(((Number) uniCount.get(0)[1]).longValue()).isEqualTo(2L);
    }
}
