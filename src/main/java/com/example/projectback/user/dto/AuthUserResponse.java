package com.example.projectback.user.dto;

import com.example.projectback.entity.User;
import com.example.projectback.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuthUserResponse {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String gender;
    private String cohort;
    private String profileImageUrl;
    private Integer trustScore;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private UserRole role;

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getGender(),
                user.getCohort(),
                user.getProfileImageUrl(),
                user.getTrustScore(),
                user.getIsVerified(),
                user.getCreatedAt(),
                user.getRole()
        );
    }
}