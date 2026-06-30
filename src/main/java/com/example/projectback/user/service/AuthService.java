package com.example.projectback.user.service;

import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.entity.User;
import com.example.projectback.security.FirebaseUserPrincipal;
import com.example.projectback.user.dto.AuthMeResponse;
import com.example.projectback.user.dto.AuthUserResponse;
import com.example.projectback.user.dto.ProfileCreateRequest;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AuthMeResponse getCurrentUser(FirebaseUserPrincipal principal) {
        return userRepository.findByFirebaseUid(principal.getFirebaseUid())
                .map(user -> AuthMeResponse.authenticated(AuthUserResponse.from(user)))
                .orElseGet(() -> AuthMeResponse.profileRequired(
                        principal.getEmail(),
                        principal.getName(),
                        principal.getProfileImageUrl()
                ));
    }

    @Transactional
    public AuthMeResponse createProfile(FirebaseUserPrincipal principal, ProfileCreateRequest request) {
        validateDuplicateUser(principal, request);

        User user = User.builder()
                .firebaseUid(principal.getFirebaseUid())
                .name(request.getName().trim())
                .nickname(request.getNickname().trim())
                .email(principal.getEmail().trim())
                .gender(request.getGender())
                .cohort(request.getCohort().trim())
                .profileImageUrl(resolveProfileImageUrl(principal, request))
                .isVerified(resolveInitialVerified(principal))
                .build();

        User savedUser = userRepository.save(user);
        return AuthMeResponse.authenticated(AuthUserResponse.from(savedUser));
    }

    @Transactional
    public AuthMeResponse verifyEmail(FirebaseUserPrincipal principal) {
        User user = userRepository.findByFirebaseUid(principal.getFirebaseUid())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("프로필 등록이 필요합니다."));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return AuthMeResponse.authenticated(AuthUserResponse.from(user));
        }

        if (!principal.isEmailVerified()) {
            throw new AccessDeniedException("이메일 인증이 아직 완료되지 않았습니다.");
        }

        user.verify();
        return AuthMeResponse.authenticated(AuthUserResponse.from(user));
    }

    private void validateDuplicateUser(FirebaseUserPrincipal principal, ProfileCreateRequest request) {
        if (!StringUtils.hasText(principal.getEmail())) {
            throw new IllegalArgumentException("Firebase 계정에 이메일 정보가 없습니다.");
        }

        if (userRepository.existsByFirebaseUid(principal.getFirebaseUid())) {
            throw new DuplicateResourceException("이미 가입이 완료된 계정입니다.");
        }

        if (userRepository.existsByEmail(principal.getEmail().trim())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname().trim())) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
    }

    private String resolveProfileImageUrl(FirebaseUserPrincipal principal, ProfileCreateRequest request) {
        if (StringUtils.hasText(request.getProfileImageUrl())) {
            return request.getProfileImageUrl().trim();
        }
        return principal.getProfileImageUrl();
    }

    private boolean resolveInitialVerified(FirebaseUserPrincipal principal) {
        return "google.com".equals(principal.getSignInProvider()) || principal.isEmailVerified();
    }

}
