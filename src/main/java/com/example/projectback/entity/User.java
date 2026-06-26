package com.example.projectback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public static final int DEFAULT_TRUST_SCORE = 50;
    private static final int MIN_TRUST_SCORE = 0;
    private static final int MAX_TRUST_SCORE = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String firebaseUid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    private String profileImageUrl;

    @Column(nullable = false)
    private String cohort;

    @Column(nullable = false, length = 1)
    private String gender;

    @Builder.Default
    @Column(nullable = false)
    private Integer trustScore = DEFAULT_TRUST_SCORE;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.trustScore == null) {
            this.trustScore = DEFAULT_TRUST_SCORE;
        }
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String cohort, String gender) {
        this.nickname = nickname;
        this.cohort = cohort;
        this.gender = gender;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void applyTrustScoreDelta(int delta) {
        int currentScore = this.trustScore == null ? DEFAULT_TRUST_SCORE : this.trustScore;
        this.trustScore = Math.max(MIN_TRUST_SCORE, Math.min(MAX_TRUST_SCORE, currentScore + delta));
    }
}

