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
import com.google.firebase.auth.FirebaseToken;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserWithdrawalServiceTest {

    @Mock CurrentUserProvider currentUserProvider;
    @Mock UserRepository userRepository;
    @Mock BoardPostRepository boardPostRepository;
    @Mock BoardCommentRepository boardCommentRepository;
    @Mock BoardPostLikeRepository boardPostLikeRepository;
    @Mock BoardCommentLikeRepository boardCommentLikeRepository;
    @Mock BoardPostReportRepository boardPostReportRepository;
    @Mock BoardCommentReportRepository boardCommentReportRepository;
    @Mock ProductRepository productRepository;
    @Mock ProductService productService;
    @Mock ProductFavoriteRepository productFavoriteRepository;
    @Mock UserReportRepository userReportRepository;
    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatMessageRepository chatMessageRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock PointLogRepository pointLogRepository;
    @Mock ImageStorageService imageStorageService;
    @Mock FirebaseAppProvider firebaseAppProvider;
    @Mock FirebaseTokenVerifier firebaseTokenVerifier;
    @Mock EntityManager entityManager;

    @InjectMocks
    UserWithdrawalService userWithdrawalService;

    @Test
    @DisplayName("withdrawUser - 장터/활동 데이터는 삭제하고 낙서판 작성자 FK만 비운 뒤 유저를 삭제한다")
    void withdrawUser_deletes_related_data_and_preserves_board_content() {
        User user = User.builder()
                .firebaseUid("")
                .name("홍길동")
                .nickname("길동")
                .email("user@example.com")
                .profileImageUrl("http://localhost:8080/uploads/profile.png")
                .gender("M")
                .cohort("30기")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(chatRoomRepository.findIdsByUserId(1L)).willReturn(List.of(10L));
        given(transactionRepository.findIdsByChatRoomIdIn(List.of(10L))).willReturn(List.of(20L));
        given(transactionRepository.findIdsByUserId(1L)).willReturn(List.of(21L, 20L));
        given(productRepository.findIdsBySellerId(1L)).willReturn(List.of(30L));

        userWithdrawalService.withdrawUser(1L);

        verify(reviewRepository).deleteByTransactionIdIn(List.of(20L, 21L));
        verify(transactionRepository).deleteByIdIn(List.of(20L, 21L));
        verify(chatMessageRepository).deleteByChatRoomIdIn(List.of(10L));
        verify(chatRoomRepository).deleteByIdIn(List.of(10L));
        verify(productService).adminDeleteProduct(30L);
        verify(reviewRepository).deleteByReviewerIdOrRevieweeId(1L, 1L);
        verify(boardCommentLikeRepository).deleteByUserId(1L);
        verify(boardCommentReportRepository).deleteByUserId(1L);
        verify(boardPostLikeRepository).deleteByUserId(1L);
        verify(boardPostReportRepository).deleteByUserId(1L);
        verify(productFavoriteRepository).deleteByUserId(1L);
        verify(userReportRepository).deleteByReporterId(1L);
        verify(userReportRepository).deleteByReportedUserId(1L);
        verify(pointLogRepository).deleteByUserId(1L);
        InOrder deleteOrder = inOrder(entityManager, boardCommentRepository, boardPostRepository, imageStorageService, userRepository);
        deleteOrder.verify(entityManager).flush();
        deleteOrder.verify(boardCommentRepository).clearAuthorByAuthorId(1L);
        deleteOrder.verify(boardPostRepository).clearAuthorByAuthorId(1L);
        deleteOrder.verify(entityManager).flush();
        deleteOrder.verify(imageStorageService).delete("http://localhost:8080/uploads/profile.png");
        deleteOrder.verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("withdrawCurrentUser - SecurityContext가 없어도 별도 ID 토큰 헤더로 유저를 찾아 탈퇴한다")
    void withdrawCurrentUser_uses_id_token_header_when_security_context_is_missing() {
        User user = User.builder()
                .firebaseUid("")
                .name("홍길동")
                .nickname("길동")
                .email("user@example.com")
                .gender("M")
                .cohort("30기")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        FirebaseToken firebaseToken = mock(FirebaseToken.class);

        given(currentUserProvider.getCurrentUser()).willThrow(new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("인증 정보가 없습니다."));
        given(firebaseTokenVerifier.verify("valid-token")).willReturn(firebaseToken);
        given(firebaseToken.getUid()).willReturn("uid-1");
        given(userRepository.findByFirebaseUid("uid-1")).willReturn(Optional.of(user));
        given(chatRoomRepository.findIdsByUserId(1L)).willReturn(List.of());
        given(transactionRepository.findIdsByUserId(1L)).willReturn(List.of());
        given(productRepository.findIdsBySellerId(1L)).willReturn(List.of());

        userWithdrawalService.withdrawCurrentUser(null, "valid-token", null);

        verify(userRepository).delete(user);
    }
}
