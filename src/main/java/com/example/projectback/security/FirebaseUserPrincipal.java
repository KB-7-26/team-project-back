package com.example.projectback.security;

import com.example.projectback.entity.User;
import com.example.projectback.entity.UserRole;
import com.google.firebase.auth.FirebaseToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class FirebaseUserPrincipal {

    private final String firebaseUid;
    private final String email;
    private final String name;
    private final String profileImageUrl;
    private final User user;

    public static FirebaseUserPrincipal from(FirebaseToken firebaseToken, User user) {
        return new FirebaseUserPrincipal(
                firebaseToken.getUid(),
                firebaseToken.getEmail(),
                firebaseToken.getName(),
                firebaseToken.getPicture(),
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
        if (user.getRole() == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
