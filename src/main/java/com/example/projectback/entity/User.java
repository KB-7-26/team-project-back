package com.example.projectback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.projectback.entity.UserRole;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

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
    private Integer trustScore = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isVerified = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isSuspended = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
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

    public void suspend() {
        this.isSuspended = true;
    }

    public void unsuspend() {
        this.isSuspended = false;
    }
}
