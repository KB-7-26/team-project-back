package com.example.projectback.user.repository;

import com.example.projectback.admin.dto.CohortSummaryResponse;
import com.example.projectback.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Page<User> findByCohort(String cohort, Pageable pageable);

    @Query("SELECT new com.example.projectback.admin.dto.CohortSummaryResponse(u.cohort, COUNT(u)) " +
           "FROM User u GROUP BY u.cohort ORDER BY u.cohort DESC")
    List<CohortSummaryResponse> findCohortSummary();
}
