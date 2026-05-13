package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardPostCreateResponse createPost(Long userId, BoardPostCreateRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        BoardPost post = BoardPost.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                .viewCount(0)
                .build();

        boardPostRepository.save(post);

        return new BoardPostCreateResponse(post.getId(), post.getTitle(), post.getCreatedAt());
    }
}