package com.example.projectback.report.repository;

import com.example.projectback.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    boolean existsByReporterIdAndReportedUserIdAndProductId(Long reporterId, Long reportedUserId, Long productId);

    void deleteByProductId(Long productId);

    void deleteByReporterId(Long reporterId);

    void deleteByReportedUserId(Long reportedUserId);

    List<UserReport> findAllByOrderByCreatedAtDesc();

    long countByReportedUserId(Long reportedUserId);

    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.product.seller.id = :userId")
    long countByProductSellerId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.reportedUser.id FROM UserReport r")
    List<Long> findDistinctReportedUserIds();
}
