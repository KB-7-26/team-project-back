package com.example.projectback.report.repository;

import com.example.projectback.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    boolean existsByReporterIdAndReportedUserIdAndProductId(Long reporterId, Long reportedUserId, Long productId);

    void deleteByProductId(Long productId);

    void deleteByReporterId(Long reporterId);

    void deleteByReportedUserId(Long reportedUserId);

    java.util.List<com.example.projectback.entity.UserReport> findAllByOrderByCreatedAtDesc();
}
