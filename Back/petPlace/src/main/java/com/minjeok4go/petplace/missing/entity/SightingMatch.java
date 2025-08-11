package com.minjeok4go.petplace.missing.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sighting_matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SightingMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sighting_id", nullable = false)
    private Sighting sighting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_report_id", nullable = false)
    private MissingReport missingReport;

    @Column(name = "score", nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status = MatchStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public SightingMatch(Sighting sighting, MissingReport missingReport, BigDecimal score) {
        this.sighting = sighting;
        this.missingReport = missingReport;
        this.score = score;
    }

    public void updateStatus(MatchStatus status) {
        this.status = status;
    }

    public enum MatchStatus {
        PENDING, CONFIRMED, REJECTED
    }
}