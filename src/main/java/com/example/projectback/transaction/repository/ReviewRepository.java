package com.example.projectback.transaction.repository;

import com.example.projectback.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 이미 리뷰를 작성했는지 확인 (중복 방지)
    boolean existsByTransactionIdAndReviewerId(Long transactionId, Long reviewerId);

    // 특정 거래의 리뷰 조회
    Optional<Review> findByTransactionId(Long transactionId);

    void deleteByTransactionIdIn(List<Long> transactionIds);

    void deleteByReviewerIdOrRevieweeId(Long reviewerId, Long revieweeId);
}
