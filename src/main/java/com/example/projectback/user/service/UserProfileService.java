package com.example.projectback.user.service;

import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.entity.User;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.user.dto.UserProfileResponse;
import com.example.projectback.user.dto.UserProfileStatsResponse;
import com.example.projectback.user.dto.UserProfileUpdateRequest;
import com.example.projectback.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final String SALE_STATUS_AVAILABLE = "available";
    private static final String SALE_STATUS_SOLD = "sold";

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return UserProfileResponse.from(user, getProfileStats(user.getId()));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        String nickname = request.getNickname().trim();
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        String cohort = request.getCohort().trim();
        String gender = request.getGender().trim();

        validateDuplicateProfile(user.getId(), nickname, phoneNumber);
        user.updateProfile(nickname, phoneNumber, cohort, gender);

        return UserProfileResponse.from(user, getProfileStats(user.getId()));
    }

    private User getCurrentUser() {
        Long userId = currentUserProvider.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private UserProfileStatsResponse getProfileStats(Long userId) {
        return new UserProfileStatsResponse(
                productRepository.countBySellerIdAndSaleStatus(userId, SALE_STATUS_AVAILABLE),
                productRepository.countBySellerIdAndSaleStatus(userId, SALE_STATUS_SOLD),
                productFavoriteRepository.countByUserId(userId)
        );
    }

    private void validateDuplicateProfile(Long userId, String nickname, String phoneNumber) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }

        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId)) {
            throw new DuplicateResourceException("이미 사용 중인 전화번호입니다.");
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.trim().replace("-", "");
    }
}
