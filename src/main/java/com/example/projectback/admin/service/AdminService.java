package com.example.projectback.admin.service;

import com.example.projectback.admin.dto.AdminReportListResponse;
import com.example.projectback.admin.dto.AdminUserResponse;
import com.example.projectback.admin.dto.CohortSummaryResponse;
import com.example.projectback.admin.dto.ReportsByCohortResponse;
import com.example.projectback.admin.dto.UserActivitySummaryResponse;
import com.example.projectback.admin.dto.UserReportSummaryResponse;
import com.example.projectback.board.repository.BoardCommentReportRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostReportRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserReport;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.product.service.ProductService;
import com.example.projectback.report.UserReportStatus;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.transaction.repository.TransactionRepository;
import com.example.projectback.user.repository.UserRepository;
import com.example.projectback.user.service.UserWithdrawalService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentReportRepository boardCommentReportRepository;
    private final BoardPostReportRepository boardPostReportRepository;
    private final ProductRepository productRepository;
    private final BoardPostService boardPostService;
    private final ProductService productService;
    private final TransactionRepository transactionRepository;
    private final UserWithdrawalService userWithdrawalService;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(Pageable pageable, String cohort) {
        if (cohort != null && !cohort.isBlank()) {
            return userRepository.findByCohort(cohort, pageable).map(AdminUserResponse::from);
        }
        return userRepository.findAll(pageable).map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse toggleSuspend(Long userId) {
        User user = getUserOrThrow(userId);
        if (Boolean.TRUE.equals(user.getIsSuspended())) {
            user.unsuspend();
        } else {
            user.suspend();
        }
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void forceWithdraw(Long userId) {
        userWithdrawalService.withdrawUser(userId);
    }

    @Transactional(readOnly = true)
    public AdminReportListResponse getReports() {
        List<UserReport> all = userReportRepository.findAllByOrderByCreatedAtDesc();
        long pendingCount = all.stream()
                .filter(r -> r.getStatus() == UserReportStatus.PENDING)
                .count();
        List<AdminReportListResponse.UserReportItem> items = all.stream()
                .map(AdminReportListResponse.UserReportItem::new)
                .toList();
        return new AdminReportListResponse(all.size(), pendingCount, items);
    }

    @Transactional
    public void adminDeletePost(Long postId) {
        boardPostService.adminDeletePost(postId);
    }

    @Transactional
    public void adminDeleteProduct(Long productId) {
        productService.adminDeleteProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<ReportsByCohortResponse> getReportsByCohort() {
        Set<Long> reportedUserIds = new HashSet<>();
        reportedUserIds.addAll(userReportRepository.findDistinctReportedUserIds());
        reportedUserIds.addAll(boardPostReportRepository.findDistinctReportedAuthorIds());
        reportedUserIds.addAll(boardCommentReportRepository.findDistinctReportedAuthorIds());

        if (reportedUserIds.isEmpty()) return List.of();

        List<User> reportedUsers = userRepository.findAllById(reportedUserIds);

        record UserTotal(User user, int total) {}

        List<UserTotal> userTotals = reportedUsers.stream()
                .map(user -> {
                    int product = (int) userReportRepository.countByProductSellerId(user.getId());
                    int post    = (int) boardPostReportRepository.countByPostAuthorId(user.getId());
                    int comment = (int) boardCommentReportRepository.countByCommentAuthorId(user.getId());
                    int userRep = (int) userReportRepository.countByReportedUserId(user.getId());
                    return new UserTotal(user, product + post + comment + userRep);
                })
                .filter(ut -> ut.total() > 0)
                .toList();

        return userTotals.stream()
                .collect(Collectors.groupingBy(ut -> ut.user().getCohort()))
                .entrySet().stream()
                .map(entry -> {
                    List<UserTotal> cohortUsers = entry.getValue();
                    int totalReportCount = cohortUsers.stream().mapToInt(UserTotal::total).sum();
                    List<ReportsByCohortResponse.ReportedUserSummary> summaries = cohortUsers.stream()
                            .map(ut -> new ReportsByCohortResponse.ReportedUserSummary(ut.user(), ut.total()))
                            .toList();
                    return new ReportsByCohortResponse(entry.getKey(), cohortUsers.size(), totalReportCount, summaries);
                })
                .sorted(Comparator.comparing(ReportsByCohortResponse::getCohort).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public UserReportSummaryResponse getUserReportSummary(Long userId) {
        int product = (int) userReportRepository.countByProductSellerId(userId);
        int post    = (int) boardPostReportRepository.countByPostAuthorId(userId);
        int comment = (int) boardCommentReportRepository.countByCommentAuthorId(userId);
        int user    = (int) userReportRepository.countByReportedUserId(userId);
        return new UserReportSummaryResponse(product, post, comment, user);
    }

    @Transactional(readOnly = true)
    public List<CohortSummaryResponse> getCohortSummary() {
        return userRepository.findCohortSummary();
    }

    @Transactional(readOnly = true)
    public UserActivitySummaryResponse getUserActivitySummary(Long userId) {
        long productCount  = productRepository.countBySellerId(userId);
        long salesCount    = transactionRepository.countBySellerIdAndStatus(userId, "completed");
        long purchaseCount = transactionRepository.countByBuyerIdAndStatus(userId, "completed");
        long postCount     = boardPostRepository.countByAuthorId(userId);
        long commentCount  = boardCommentRepository.countByAuthorId(userId);
        return new UserActivitySummaryResponse(productCount, salesCount, purchaseCount, postCount, commentCount);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }
}
