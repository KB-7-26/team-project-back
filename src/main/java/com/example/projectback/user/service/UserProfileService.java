package com.example.projectback.user.service;

import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.User;
import com.example.projectback.image.service.ImageStorageService;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.user.dto.UserProfileResponse;
import com.example.projectback.user.dto.UserProfileStatsResponse;
import com.example.projectback.user.dto.UserProfileUpdateRequest;
import com.example.projectback.user.dto.UserPublicProfileResponse;
import com.example.projectback.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final String SALE_STATUS_AVAILABLE = "available";
    private static final String SALE_STATUS_SOLD = "sold";
    private static final String SALE_STATUS_COMPLETED_LEGACY = "completed";

    private final UserRepository userRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final ProductRepository productRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return UserProfileResponse.from(user, getProfileStats(user.getId()));
    }

    @Transactional(readOnly = true)
    public UserPublicProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return UserPublicProfileResponse.from(user, getProfileStats(user.getId()));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        String nickname = request.getNickname().trim();

        validateDuplicateProfile(user.getId(), nickname);
        user.updateNickname(nickname);

        return UserProfileResponse.from(user, getProfileStats(user.getId()));
    }

    @Transactional
    public UserProfileResponse uploadMyProfileImage(MultipartFile image) {
        User user = getCurrentUser();
        String previousImageUrl = user.getProfileImageUrl();
        String imageUrl = imageStorageService.store(image);

        user.updateProfileImageUrl(imageUrl);

        if (previousImageUrl != null && !previousImageUrl.equals(imageUrl)) {
            imageStorageService.delete(previousImageUrl);
        }

        return UserProfileResponse.from(user, getProfileStats(user.getId()));
    }

    @Transactional
    public UserProfileResponse deleteMyProfileImage() {
        User user = getCurrentUser();
        String previousImageUrl = user.getProfileImageUrl();

        user.updateProfileImageUrl(null);

        if (previousImageUrl != null) {
            imageStorageService.delete(previousImageUrl);
        }

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
                productRepository.countBySellerIdAndSaleStatusIn(userId, List.of(SALE_STATUS_SOLD, SALE_STATUS_COMPLETED_LEGACY)),
                productFavoriteRepository.countByUserId(userId),
                boardPostRepository.countByAuthorId(userId),
                boardCommentRepository.countByAuthorId(userId)
        );
    }

    private void validateDuplicateProfile(Long userId, String nickname) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
    }
}
