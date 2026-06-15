package com.example.projectback.user.service;

import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.entity.User;
import com.example.projectback.security.FirebaseUserPrincipal;
import com.example.projectback.user.dto.AuthMeResponse;
import com.example.projectback.user.dto.ProfileCreateRequest;
import com.example.projectback.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void createProfileSavesFirebaseUserProfile() {
        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                "firebase-uid-1",
                "user01@example.com",
                "홍길동",
                "https://example.com/profile.png",
                false,
                "password",
                null
        );
        ProfileCreateRequest request = new ProfileCreateRequest(
                "홍길동",
                "길동",
                "M",
                "30회차 비전공",
                null
        );

        given(userRepository.existsByFirebaseUid("firebase-uid-1")).willReturn(false);
        given(userRepository.existsByEmail("user01@example.com")).willReturn(false);
        given(userRepository.existsByNickname("길동")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        authService.createProfile(principal, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirebaseUid()).isEqualTo("firebase-uid-1");
        assertThat(savedUser.getEmail()).isEqualTo("user01@example.com");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getNickname()).isEqualTo("길동");
        assertThat(savedUser.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(savedUser.getTrustScore()).isZero();
        assertThat(savedUser.getIsVerified()).isFalse();
    }

    @Test
    void createProfileRejectsDuplicateFirebaseUid() {
        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                "firebase-uid-1",
                "user01@example.com",
                "홍길동",
                null,
                false,
                "password",
                null
        );
        ProfileCreateRequest request = new ProfileCreateRequest(
                "홍길동",
                "길동",
                "M",
                "30회차 비전공",
                null
        );

        given(userRepository.existsByFirebaseUid("firebase-uid-1")).willReturn(true);

        assertThatThrownBy(() -> authService.createProfile(principal, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("이미 가입이 완료된 계정입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentUserReturnsProfileRequiredWhenUserDoesNotExist() {
        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                "firebase-uid-1",
                "user01@example.com",
                "홍길동",
                null,
                false,
                "password",
                null
        );
        given(userRepository.findByFirebaseUid("firebase-uid-1")).willReturn(Optional.empty());

        AuthMeResponse response = authService.getCurrentUser(principal);

        assertThat(response.getProfileRequired()).isTrue();
        assertThat(response.getPendingUser().getEmail()).isEqualTo("user01@example.com");
        assertThat(response.getUser()).isNull();
    }

    @Test
    void createProfileMarksGoogleAccountVerified() {
        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                "firebase-uid-1",
                "user01@example.com",
                "홍길동",
                "https://example.com/profile.png",
                false,
                "google.com",
                null
        );
        ProfileCreateRequest request = new ProfileCreateRequest(
                "홍길동",
                "길동",
                "M",
                "30회차 비전공",
                null
        );

        given(userRepository.existsByFirebaseUid("firebase-uid-1")).willReturn(false);
        given(userRepository.existsByEmail("user01@example.com")).willReturn(false);
        given(userRepository.existsByNickname("길동")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        authService.createProfile(principal, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getIsVerified()).isTrue();
    }

    @Test
    void verifyEmailMarksEmailAccountVerified() {
        User user = User.builder()
                .firebaseUid("firebase-uid-1")
                .email("user01@example.com")
                .name("홍길동")
                .nickname("길동")
                .gender("M")
                .cohort("30회차 비전공")
                .isVerified(false)
                .build();
        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                "firebase-uid-1",
                "user01@example.com",
                "홍길동",
                null,
                true,
                "password",
                user
        );
        given(userRepository.findByFirebaseUid("firebase-uid-1")).willReturn(Optional.of(user));

        AuthMeResponse response = authService.verifyEmail(principal);

        assertThat(user.getIsVerified()).isTrue();
        assertThat(response.getUser().getIsVerified()).isTrue();
    }

    @Test
    void verifyEmailRejectsUnverifiedFirebaseEmail() {
        User user = User.builder()
                .firebaseUid("firebase-uid-1")
                .email("user01@example.com")
                .name("홍길동")
                .nickname("길동")
                .gender("M")
                .cohort("30회차 비전공")
                .isVerified(false)
                .build();
        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                "firebase-uid-1",
                "user01@example.com",
                "홍길동",
                null,
                false,
                "password",
                user
        );
        given(userRepository.findByFirebaseUid("firebase-uid-1")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail(principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("이메일 인증이 아직 완료되지 않았습니다.");

        assertThat(user.getIsVerified()).isFalse();
    }
}
