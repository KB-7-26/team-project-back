package com.example.projectback.user.repository;

import com.example.projectback.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {

    void deleteByUserId(Long userId);
}
