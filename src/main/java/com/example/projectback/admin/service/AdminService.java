package com.example.projectback.admin.service;

import com.example.projectback.admin.dto.AdminReportListResponse;
import com.example.projectback.admin.dto.AdminUserResponse;
import com.example.projectback.board.repository.BoardCommentLikeRepository;
import com.example.projectback.board.repository.BoardCommentReportRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostReportRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserReport;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.product.service.ProductService;
import com.example.projectback.report.UserReportStatus;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardCommentReportRepository boardCommentReportRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardPostReportRepository boardPostReportRepository;
    private final ProductRepository productRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final BoardPostService boardPostService;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(Pageable pageable) {
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
        User user = getUserOrThrow(userId);

        // 이 유저가 작성한 댓글의 ID + 그 댓글의 자식 댓글 ID (다른 유저가 달았지만 cascade 삭제 대상)
        List<Long> userCommentIds = boardCommentRepository.findIdsByAuthorId(userId);
        List<Long> childOfUserCommentIds = boardCommentRepository.findReplyIdsByParentAuthorId(userId);
        List<Long> allAffectedCommentIds = new ArrayList<>(userCommentIds);
        allAffectedCommentIds.addAll(childOfUserCommentIds);
        if (!allAffectedCommentIds.isEmpty()) {
            boardCommentLikeRepository.deleteByCommentIdIn(allAffectedCommentIds);
            boardCommentReportRepository.deleteByCommentIdIn(allAffectedCommentIds);
        }
        boardCommentRepository.deleteRepliesByAuthorId(userId);
        boardCommentRepository.deleteParentsByAuthorId(userId); // cascade로 자식 댓글도 삭제

        // 이 유저의 게시글 삭제 (댓글/좋아요/신고 포함)
        boardPostRepository.findIdsByAuthorId(userId).forEach(boardPostService::adminDeletePost);

        // 게시글 좋아요/신고 (다른 글에 누른 것)
        boardPostLikeRepository.deleteByUserId(userId);
        boardPostReportRepository.deleteByUserId(userId);

        // 상품 삭제 (이미지/찜/신고 포함)
        productRepository.findIdsBySellerId(userId).forEach(productService::adminDeleteProduct);

        // 다른 사람 상품에 찜한 것
        productFavoriteRepository.deleteByUserId(userId);

        // 유저 신고 (신고자/피신고자 모두)
        userReportRepository.deleteByReporterId(userId);
        userReportRepository.deleteByReportedUserId(userId);

        userRepository.delete(user);
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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }
}