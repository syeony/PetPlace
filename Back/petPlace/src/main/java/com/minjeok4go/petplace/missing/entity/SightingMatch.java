package com.minjeok4go.petplace.missing.entity;

import com.minjeok4go.petplace.image.entity.Image;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id") // 처음엔 nullable 허용 권장
    private Image image;

    @Column(name = "score", nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status = MatchStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public SightingMatch(Sighting sighting, MissingReport missingReport, Image image,BigDecimal score) {
        this.sighting = sighting;
        this.missingReport = missingReport;
        this.image = image;
        this.score = score;
    }

    public void updateStatus(MatchStatus status) {
        this.status = status;
    }

    public enum MatchStatus {
        PENDING, CONFIRMED, REJECTED
    }
}