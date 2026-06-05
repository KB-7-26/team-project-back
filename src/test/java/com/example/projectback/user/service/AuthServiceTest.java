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
                null
        );
        given(userRepository.findByFirebaseUid("firebase-uid-1")).willReturn(Optional.empty());

        AuthMeResponse response = authService.getCurrentUser(principal);

        assertThat(response.getProfileRequired()).isTrue();
        assertThat(response.getPendingUser().getEmail()).isEqualTo("user01@example.com");
        assertThat(response.getUser()).isNull();
    }
}
