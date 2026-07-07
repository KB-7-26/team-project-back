package com.example.projectback.user.service;

import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostImageRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.user.dto.BoardEventDailyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserBoardActivityService {

    private static final int EVENT_MONTH = 7;
    private static final int EVENT_START_DAY = 7;
    private static final int EVENT_DAY_COUNT = 4;

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardPostImageRepository boardPostImageRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<BoardPostListItemResponse> getMyPosts(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        return boardPostRepository.findByAuthorId(userId, pageable)
                .map(post -> new BoardPostListItemResponse(
                        post,
                        boardCommentRepository.countByPostId(post.getId()),
                        boardPostLikeRepository.countByPostId(post.getId()),
                        boardPostImageRepository.countByPostId(post.getId()) > 0
                ));
    }

    @Transactional(readOnly = true)
    public Page<BoardPostListItemResponse> getMyCommentedPosts(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        return boardPostRepository.findCommentedPostsByAuthorId(userId, pageable)
                .map(post -> new BoardPostListItemResponse(
                        post,
                        boardCommentRepository.countByPostId(post.getId()),
                        boardPostLikeRepository.countByPostId(post.getId()),
                        boardPostImageRepository.countByPostId(post.getId()) > 0
                ));
    }

    @Transactional(readOnly = true)
    public Page<BoardPostListItemResponse> getMyLikedPosts(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        return boardPostRepository.findLikedPostsByUserId(userId, pageable)
                .map(post -> new BoardPostListItemResponse(
                        post,
                        boardCommentRepository.countByPostId(post.getId()),
                        boardPostLikeRepository.countByPostId(post.getId())
                ));
    }

    @Transactional(readOnly = true)
    public List<BoardEventDailyStatsResponse> getBoardEventDailyStats() {
        Long userId = currentUserProvider.getCurrentUserId();
        int eventYear = LocalDate.now().getYear();

        return IntStream.range(0, EVENT_DAY_COUNT)
                .mapToObj(dayOffset -> {
                    LocalDate date = LocalDate.of(eventYear, EVENT_MONTH, EVENT_START_DAY + dayOffset);
                    LocalDateTime startAt = date.atStartOfDay();
                    LocalDateTime endAt = date.plusDays(1).atStartOfDay();
                    long postCount = boardPostRepository
                            .countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, startAt, endAt);
                    long commentCount = boardCommentRepository
                            .countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, startAt, endAt);

                    return new BoardEventDailyStatsResponse(date, postCount, commentCount);
                })
                .toList();
    }
}
