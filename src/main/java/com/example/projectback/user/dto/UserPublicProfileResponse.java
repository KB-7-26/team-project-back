package com.example.projectback.user.dto;

import com.example.projectback.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserPublicProfileResponse {

    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String cohort;
    private String gender;
    private Integer trustScore;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private UserProfileStatsResponse stats;

    public static UserPublicProfileResponse from(User user, UserProfileStatsResponse stats) {
        return new UserPublicProfileResponse(
                user.getId(),
                user.getNickname(),
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
