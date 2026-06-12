package com.example.projectback.report.service;

import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.Product;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserReport;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserReportService {

    private final CurrentUserProvider currentUserProvider;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserReportRepository userReportRepository;

    @Transactional
    public void reportProductUser(Long productId, Long reportedUserId) {
        User reporter = currentUserProvider.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        User reportedUser = resolveReportedUser(product, reportedUserId);

        if (reporter.getId().equals(reportedUser.getId())) {
            throw new IllegalArgumentException("본인은 신고할 수 없습니다.");
        }

        validateMarketReportTarget(reporter, reportedUser, product);

        if (userReportRepository.existsByReporterIdAndReportedUserIdAndProductId(
                reporter.getId(),
                reportedUser.getId(),
                product.getId()
        )) {
            return;
        }

        try {
            userReportRepository.save(UserReport.create(reporter, reportedUser, product));
        } catch (DataIntegrityViolationException ignored) {
            // 동일 신고 동시 요청은 unique 제약으로 멱등 처리합니다.
        }
    }

    private User resolveReportedUser(Product product, Long reportedUserId) {
        if (reportedUserId == null) {
            return product.getSeller();
        }

        if (product.getSeller().getId().equals(reportedUserId)) {
            return product.getSeller();
        }

        return userRepository.findById(reportedUserId)
                .orElseThrow(() -> new EntityNotFoundException("신고 대상 사용자를 찾을 수 없습니다."));
    }

    private void validateMarketReportTarget(User reporter, User reportedUser, Product product) {
        if (reportedUser.getId().equals(product.getSeller().getId())) {
            return;
        }

        boolean reporterIsSeller = reporter.getId().equals(product.getSeller().getId());
        boolean reportedUserIsProductBuyer =
                chatRoomRepository.existsByProductIdAndBuyerId(product.getId(), reportedUser.getId());

        if (!reporterIsSeller || !reportedUserIsProductBuyer) {
            throw new IllegalArgumentException("해당 거래의 신고 대상이 아닙니다.");
        }
    }
}
