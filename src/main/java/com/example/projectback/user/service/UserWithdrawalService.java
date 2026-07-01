package com.example.projectback.user.service;

import com.example.projectback.board.repository.BoardCommentLikeRepository;
import com.example.projectback.board.repository.BoardCommentReportRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostReportRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.chat.repository.ChatMessageRepository;
import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.User;
import com.example.projectback.image.service.ImageStorageService;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.product.service.ProductService;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.security.FirebaseAppProvider;
import com.example.projectback.security.FirebaseTokenVerifier;
import com.example.projectback.transaction.repository.ReviewRepository;
import com.example.projectback.transaction.repository.TransactionRepository;
import com.example.projectback.user.repository.PointLogRepository;
import com.example.projectback.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardPostReportRepository boardPostReportRepository;
    private final BoardCommentReportRepository boardCommentReportRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final UserReportRepository userReportRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final PointLogRepository pointLogRepository;
    private final ImageStorageService imageStorageService;
    private final FirebaseAppProvider firebaseAppProvider;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final EntityManager entityManager;

    @Transactional
    public void withdrawCurrentUser() {
        withdrawUser(currentUserProvider.getCurrentUser());
    }

    @Transactional
    public void withdrawCurrentUser(String authorizationHeader) {
        withdrawCurrentUser(authorizationHeader, null, null);
    }

    @Transactional
    public void withdrawCurrentUser(String authorizationHeader, String idTokenHeader, String idToken) {
        User authenticatedUser = getAuthenticatedUserOrNull();
        if (authenticatedUser != null) {
            withdrawUser(authenticatedUser);
            return;
        }

        FirebaseToken firebaseToken = firebaseTokenVerifier.verify(resolveToken(authorizationHeader, idTokenHeader, idToken));
        User user = userRepository.findByFirebaseUid(firebaseToken.getUid())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("프로필 등록이 필요합니다."));
        withdrawUser(user);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        withdrawUser(user);
    }

    private void withdrawUser(User user) {
        Long userId = user.getId();
        String firebaseUid = user.getFirebaseUid();
        String profileImageUrl = user.getProfileImageUrl();

        deleteMarketAndChatData(userId);
        deleteUserActivityData(userId);

        // The board bulk updates below clear the persistence context.
        // Flush queued deletes first so product/chat/activity deletions are not discarded.
        entityManager.flush();

        boardCommentRepository.clearAuthorByAuthorId(userId);
        boardPostRepository.clearAuthorByAuthorId(userId);
        entityManager.flush();

        if (StringUtils.hasText(profileImageUrl)) {
            imageStorageService.delete(profileImageUrl);
        }

        userRepository.delete(user);
        registerFirebaseUserDeletion(firebaseUid);
    }

    private void deleteMarketAndChatData(Long userId) {
        List<Long> chatRoomIds = chatRoomRepository.findIdsByUserId(userId);
        deleteTransactionsAndReviews(collectTransactionIds(userId, chatRoomIds));

        if (!chatRoomIds.isEmpty()) {
            chatMessageRepository.deleteByChatRoomIdIn(chatRoomIds);
            chatRoomRepository.deleteByIdIn(chatRoomIds);
        }

        productRepository.findIdsBySellerId(userId).forEach(productService::adminDeleteProduct);
    }

    private List<Long> collectTransactionIds(Long userId, List<Long> chatRoomIds) {
        Set<Long> transactionIds = new LinkedHashSet<>();
        if (!chatRoomIds.isEmpty()) {
            transactionIds.addAll(transactionRepository.findIdsByChatRoomIdIn(chatRoomIds));
        }
        transactionIds.addAll(transactionRepository.findIdsByUserId(userId));
        return new ArrayList<>(transactionIds);
    }

    private void deleteTransactionsAndReviews(List<Long> transactionIds) {
        if (!transactionIds.isEmpty()) {
            reviewRepository.deleteByTransactionIdIn(transactionIds);
            transactionRepository.deleteByIdIn(transactionIds);
        }
    }

    private void deleteUserActivityData(Long userId) {
        reviewRepository.deleteByReviewerIdOrRevieweeId(userId, userId);
        boardCommentLikeRepository.deleteByUserId(userId);
        boardCommentReportRepository.deleteByUserId(userId);
        boardPostLikeRepository.deleteByUserId(userId);
        boardPostReportRepository.deleteByUserId(userId);
        productFavoriteRepository.deleteByUserId(userId);
        userReportRepository.deleteByReporterId(userId);
        userReportRepository.deleteByReportedUserId(userId);
        pointLogRepository.deleteByUserId(userId);
    }

    private User getAuthenticatedUserOrNull() {
        try {
            return currentUserProvider.getCurrentUser();
        } catch (AuthenticationCredentialsNotFoundException exception) {
            return null;
        }
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }
        return token;
    }

    private String resolveToken(String authorizationHeader, String idTokenHeader, String idToken) {
        if (StringUtils.hasText(idTokenHeader)) {
            return idTokenHeader.trim();
        }

        if (StringUtils.hasText(idToken)) {
            return idToken.trim();
        }

        if (StringUtils.hasText(authorizationHeader)) {
            return resolveBearerToken(authorizationHeader);
        }

        throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
    }

    private void registerFirebaseUserDeletion(String firebaseUid) {
        if (!StringUtils.hasText(firebaseUid)) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    FirebaseAuth.getInstance(firebaseAppProvider.getFirebaseApp()).deleteUser(firebaseUid);
                } catch (FirebaseAuthException | IOException | RuntimeException exception) {
                    log.warn("Firebase Auth 계정 삭제 실패: firebaseUid={}", firebaseUid, exception);
                }
            }
        });
    }
}
