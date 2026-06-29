package com.example.projectback.user.repository;

import com.example.projectback.admin.dto.CohortSummaryResponse;
import com.example.projectback.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    @Modifying
    @Query(value = """
            UPDATE users u
            SET u.trust_score = 50
            WHERE (u.trust_score IS NULL OR u.trust_score = 0)
              AND NOT EXISTS (
                  SELECT 1 FROM reviews r WHERE r.reviewee_id = u.id
              )
            """, nativeQuery = true)
    int initializeDefaultTrustScoreForUsersWithoutReviews();
}

