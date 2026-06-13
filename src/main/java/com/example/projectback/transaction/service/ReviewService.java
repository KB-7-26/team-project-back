package com.example.projectback.transaction.service;

import com.example.projectback.entity.Review;
import com.example.projectback.entity.Transaction;
import com.example.projectback.entity.User;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.transaction.dto.ReviewRequest;
import com.example.projectback.transaction.dto.ReviewResponse;
import com.example.projectback.transaction.repository.ReviewRepository;
import com.example.projectback.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        // 거래 조회
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("거래를 찾을 수 없습니다."));

        // 완료된 거래인지 확인
        if (!transaction.getStatus().equals("completed")) {
            throw new IllegalStateException("완료된 거래에만 리뷰를 작성할 수 있습니다.");
        }

        // 거래 참여자인지 확인 (판매자 or 구매자만 작성 가능)
        boolean isSeller = transaction.getSeller().getId().equals(currentUser.getId());
        boolean isBuyer = transaction.getBuyer().getId().equals(currentUser.getId());
        if (!isSeller && !isBuyer) {
            throw new IllegalStateException("거래 참여자만 리뷰를 작성할 수 있습니다.");
        }

        // 중복 리뷰 방지
        if (reviewRepository.existsByTransactionIdAndReviewerId(request.getTransactionId(), currentUser.getId())) {
            throw new IllegalStateException("이미 리뷰를 작성했습니다.");
        }

        // 별점 유효성 검사
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }

        // 리뷰 대상 = 나의 상대방
        User reviewee = isSeller ? transaction.getBuyer() : transaction.getSeller();

        Review review = Review.builder()
                .transaction(transaction)
                .reviewer(currentUser)
                .reviewee(reviewee)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);

        return new ReviewResponse(
                review.getId(),
                transaction.getId(),
                currentUser.getId(),
                currentUser.getNickname(),
                reviewee.getId(),
                reviewee.getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
