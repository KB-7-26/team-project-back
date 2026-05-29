package com.example.projectback.user.dto;

import com.example.projectback.entity.User;
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
    private String phoneNumber;
    private String gender;
    private String cohort;
    private String profileImageUrl;
    private Integer trustScore;
    private Boolean isVerified;
    private LocalDateTime createdAt;

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getCohort(),
                user.getProfileImageUrl(),
                user.getTrustScore(),
                user.getIsVerified(),
                user.getCreatedAt()
        );
    }
}
