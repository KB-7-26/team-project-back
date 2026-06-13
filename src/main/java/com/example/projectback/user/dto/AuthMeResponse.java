package com.example.projectback.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMeResponse {

    private Boolean profileRequired;
    private AuthUserResponse user;
    private PendingFirebaseUserResponse pendingUser;

    public static AuthMeResponse authenticated(AuthUserResponse user) {
        return new AuthMeResponse(false, user, null);
    }

    public static AuthMeResponse profileRequired(String email, String name, String profileImageUrl) {
        return new AuthMeResponse(
                true,
                null,
                new PendingFirebaseUserResponse(email, name, profileImageUrl)
        );
    }
}
