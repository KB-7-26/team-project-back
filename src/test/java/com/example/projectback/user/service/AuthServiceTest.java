package com.example.projectback.user.service;

import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.entity.User;
import com.example.projectback.user.dto.SignupRequest;
import com.example.projectback.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupEncodesPasswordAndSavesUser() {
        SignupRequest request = new SignupRequest(
                "user01",
                "password123",
                "홍길동",
                "길동",
                "user01@example.com",
                "010-1234-5678",
                "M",
                "30회차 비전공"
        );

        given(userRepository.existsByLoginId("user01")).willReturn(false);
        given(userRepository.existsByEmail("user01@example.com")).willReturn(false);
        given(userRepository.existsByPhoneNumber("01012345678")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        authService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getLoginId()).isEqualTo("user01");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getEmail()).isEqualTo("user01@example.com");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(savedUser.getTrustScore()).isZero();
        assertThat(savedUser.getIsVerified()).isFalse();
    }

    @Test
    void signupRejectsDuplicateLoginId() {
        SignupRequest request = new SignupRequest(
                "user01",
                "password123",
                "홍길동",
                "길동",
                "user01@example.com",
                null,
                "M",
                "30회차 비전공"
        );

        given(userRepository.existsByLoginId("user01")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
