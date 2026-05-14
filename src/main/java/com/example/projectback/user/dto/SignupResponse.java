package com.example.projectback.user.dto;

import com.example.projectback.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long id;
    private String loginId;
    private String name;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String gender;
    private String cohort;
    private Integer trustScore;
    private Boolean isVerified;
    private LocalDateTime createdAt;

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getCohort(),
                user.getTrustScore(),
                user.getIsVerified(),
                user.getCreatedAt()
        );
    }
}
