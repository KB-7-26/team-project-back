package com.example.projectback.security;

import com.example.projectback.entity.User;
import com.google.firebase.auth.FirebaseToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class FirebaseUserPrincipal {

    private final String firebaseUid;
    private final String email;
    private final String name;
    private final String profileImageUrl;
    private final boolean emailVerified;
    private final String signInProvider;
    private final User user;

    public static FirebaseUserPrincipal from(FirebaseToken firebaseToken, User user) {
        return new FirebaseUserPrincipal(
                firebaseToken.getUid(),
                firebaseToken.getEmail(),
                firebaseToken.getName(),
                firebaseToken.getPicture(),
                firebaseToken.isEmailVerified(),
                resolveSignInProvider(firebaseToken),
                user
        );
    }

    public boolean hasCompletedProfile() {
        return user != null;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (!hasCompletedProfile()) {
            return List.of(new SimpleGrantedAuthority("ROLE_PENDING_USER"));
        }

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return List.of(new SimpleGrantedAuthority("ROLE_UNVERIFIED_USER"));
    }

    private static String resolveSignInProvider(FirebaseToken firebaseToken) {
        Object firebaseClaims = firebaseToken.getClaims().get("firebase");
        if (firebaseClaims instanceof Map<?, ?> claims) {
            Object provider = claims.get("sign_in_provider");
            if (provider instanceof String value) {
                return value;
            }
        }
        return null;
    }
}
