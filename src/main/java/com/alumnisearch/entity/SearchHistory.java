package com.alumnisearch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "search_history",
        indexes = {
                @Index(name = "idx_search_university", columnList = "university"),
                @Index(name = "idx_search_timestamp", columnList = "searched_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String university;

    @Column(nullable = false, length = 150)
    private String designation;

    @Column(name = "passout_year")
    private Integer passoutYear;

    @Column(name = "results_count", nullable = false)
    private int resultsCount;

    @Column(name = "execution_time_ms")
    private long executionTimeMs;

    @Column(length = 50)
    private String provider;

    @CreatedDate
    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt;
}
