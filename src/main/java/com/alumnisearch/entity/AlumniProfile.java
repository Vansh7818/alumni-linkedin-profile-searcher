package com.alumnisearch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "alumni_profiles",
        indexes = {
                @Index(name = "idx_alumni_university", columnList = "university"),
                @Index(name = "idx_alumni_current_role", columnList = "job_role"),
                @Index(name = "idx_alumni_passout_year", columnList = "passout_year"),
                @Index(name = "idx_alumni_linkedin_url", columnList = "linkedin_url")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "job_role", nullable = false, length = 150)
    private String currentRole;

    @Column(nullable = false, length = 200)
    private String university;

    @Column(length = 150)
    private String location;

    @Column(name = "linkedin_headline", length = 500)
    private String linkedinHeadline;

    @Column(name = "passout_year")
    private Integer passoutYear;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(length = 150)
    private String company;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String skills;

    @Column(name = "verification_status", length = 50)
    @Builder.Default
    private String verificationStatus = "SCRAPED";

    @Column(name = "search_query", length = 255)
    private String searchQuery;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
