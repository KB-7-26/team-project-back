package com.example.projectback.admin.dto;

import com.example.projectback.entity.User;
import com.example.projectback.entity.UserRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String cohort;
    private final UserRole role;
    private final Boolean isSuspended;
    private final Integer trustScore;
    private final LocalDateTime createdAt;

    private AdminUserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.cohort = user.getCohort();
        this.role = user.getRole();
        this.isSuspended = user.getIsSuspended();
        this.trustScore = user.getTrustScore();
        this.createdAt = user.getCreatedAt();
    }

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user);
    }
}