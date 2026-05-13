package com.example.projectback.user.service;

import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.entity.User;
import com.example.projectback.user.dto.SignupRequest;
import com.example.projectback.user.dto.SignupResponse;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicateUser(request);

        User user = User.builder()
                .loginId(request.getLoginId().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .nickname(request.getNickname().trim())
                .email(request.getEmail().trim())
                .phoneNumber(normalizePhoneNumber(request.getPhoneNumber()))
                .gender(request.getGender())
                .cohort(request.getCohort().trim())
                .build();

        User savedUser = userRepository.save(user);
        return SignupResponse.from(savedUser);
    }

    private void validateDuplicateUser(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId().trim())) {
            throw new DuplicateResourceException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다.");
        }

        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateResourceException("이미 사용 중인 전화번호입니다.");
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        return phoneNumber.trim().replace("-", "");
    }
}
