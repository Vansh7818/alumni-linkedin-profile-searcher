package com.alumnisearch.repository;

import com.alumnisearch.entity.AlumniProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long>, JpaSpecificationExecutor<AlumniProfile> {

    Optional<AlumniProfile> findByLinkedinUrl(String linkedinUrl);

    Optional<AlumniProfile> findByNameAndUniversityAndCurrentRole(String name, String university, String currentRole);

    List<AlumniProfile> findByUniversityIgnoreCase(String university);

    @Query("SELECT a.university, COUNT(a) FROM AlumniProfile a GROUP BY a.university ORDER BY COUNT(a) DESC")
    List<Object[]> countAlumniByUniversity();

    @Query("SELECT a.currentRole, COUNT(a) FROM AlumniProfile a GROUP BY a.currentRole ORDER BY COUNT(a) DESC")
    List<Object[]> countAlumniByRole();

    @Query("SELECT a.passoutYear, COUNT(a) FROM AlumniProfile a WHERE a.passoutYear IS NOT NULL GROUP BY a.passoutYear ORDER BY a.passoutYear DESC")
    List<Object[]> countAlumniByPassoutYear();
}
