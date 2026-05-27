package com.example.projectback.security;

import com.example.projectback.entity.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentFirebaseUid() {
        return getCurrentUserPrincipal().getFirebaseUid();
    }

    public User getCurrentUser() {
        User user = getCurrentUserPrincipal().getUser();
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("프로필 등록이 필요합니다.");
        }
        return user;
    }

    private FirebaseUserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof FirebaseUserPrincipal userPrincipal)) {
            throw new AuthenticationCredentialsNotFoundException("인증 정보가 올바르지 않습니다.");
        }

        return userPrincipal;
    }
}
