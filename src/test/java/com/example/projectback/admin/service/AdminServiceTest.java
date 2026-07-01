package com.example.projectback.admin.service;

import com.example.projectback.admin.dto.AdminUserResponse;
import com.example.projectback.board.repository.BoardCommentReportRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostReportRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserRole;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.product.service.ProductService;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.transaction.repository.TransactionRepository;
import com.example.projectback.user.repository.UserRepository;
import com.example.projectback.user.service.UserWithdrawalService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserReportRepository userReportRepository;
    @Mock BoardPostRepository boardPostRepository;
    @Mock BoardCommentRepository boardCommentRepository;
    @Mock BoardCommentReportRepository boardCommentReportRepository;
    @Mock BoardPostReportRepository boardPostReportRepository;
    @Mock ProductRepository productRepository;
    @Mock BoardPostService boardPostService;
    @Mock ProductService productService;
    @Mock TransactionRepository transactionRepository;
    @Mock UserWithdrawalService userWithdrawalService;

    @InjectMocks
    AdminService adminService;

    private User normalUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .firebaseUid("uid-1")
                .name("홍길동")
                .nickname("길동")
                .email("user@example.com")
                .gender("M")
                .cohort("30기")
                .build();
    }

    @Test
    @DisplayName("toggleSuspend - 정지되지 않은 유저를 정지시킨다")
    void toggleSuspend_suspends_unsuspended_user() {
        given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

        AdminUserResponse response = adminService.toggleSuspend(1L);

        assertThat(response.getIsSuspended()).isTrue();
    }

    @Test
    @DisplayName("toggleSuspend - 이미 정지된 유저를 해제한다")
    void toggleSuspend_unsuspends_suspended_user() {
        normalUser.suspend();
        given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

        AdminUserResponse response = adminService.toggleSuspend(1L);

        assertThat(response.getIsSuspended()).isFalse();
    }

    @Test
    @DisplayName("toggleSuspend - 존재하지 않는 유저면 예외 발생")
    void toggleSuspend_throws_when_user_not_found() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.toggleSuspend(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("forceWithdraw - 회원 탈퇴 서비스에 위임한다")
    void forceWithdraw_delegates_to_withdrawal_service() {
        adminService.forceWithdraw(1L);

        verify(userWithdrawalService).withdrawUser(1L);
    }

    @Test
    @DisplayName("User 신규 생성 시 role은 기본값 USER")
    void newUser_hasDefaultRoleUser() {
        assertThat(normalUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("User 신규 생성 시 isSuspended는 기본값 false")
    void newUser_isNotSuspendedByDefault() {
        assertThat(normalUser.getIsSuspended()).isFalse();
    }
}
