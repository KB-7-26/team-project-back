package com.example.projectback.user.dto;

import com.example.projectback.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String cohort;
    private String gender;
    private Integer trustScore;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private UserProfileStatsResponse stats;

    public static UserProfileResponse from(User user, UserProfileStatsResponse stats) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getCohort(),
                user.getGender(),
                user.getTrustScore(),
                user.getIsVerified(),
                user.getCreatedAt(),
                stats
        );
    }
}
