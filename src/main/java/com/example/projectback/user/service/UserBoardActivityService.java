package com.example.projectback.user.service;

import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostImageRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBoardActivityService {

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
}
