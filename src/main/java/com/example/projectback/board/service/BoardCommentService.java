package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardCommentCreateRequest;
import com.example.projectback.board.dto.BoardCommentResponse;
import com.example.projectback.board.dto.BoardCommentUpdateRequest;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardComment;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BoardCommentResponse> getComments(Long postId) {
        BoardPost post = getPostOrThrow(postId);
        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Map<Long, String> anonMap = buildAnonMap(allComments, post.getAuthor().getId());

        Map<Long, List<BoardComment>> repliesByParentId = allComments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .map(c -> toResponse(c, anonMap, repliesByParentId))
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardCommentResponse createComment(Long postId, Long userId, BoardCommentCreateRequest request) {
        BoardPost post = getPostOrThrow(postId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        BoardComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = boardCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));
        }

        BoardComment comment = BoardComment.builder()
                .post(post)
                .author(author)
                .parentComment(parentComment)
                .content(request.getContent())
                .build();

        boardCommentRepository.save(comment);

        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Map<Long, String> anonMap = buildAnonMap(allComments, post.getAuthor().getId());

        return new BoardCommentResponse(comment, anonMap.get(userId), List.of());
    }

    @Transactional
    public BoardCommentResponse updateComment(Long postId, Long commentId, Long userId, BoardCommentUpdateRequest request) {
        BoardPost post = getPostOrThrow(postId);
        BoardComment comment = getCommentOrThrow(commentId);
        validateCommentAuthor(comment, userId);

        comment.update(request.getContent());

        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Map<Long, String> anonMap = buildAnonMap(allComments, post.getAuthor().getId());

        return new BoardCommentResponse(comment, anonMap.get(userId), List.of());
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        getPostOrThrow(postId);
        BoardComment comment = getCommentOrThrow(commentId);
        validateCommentAuthor(comment, userId);

        boardCommentRepository.delete(comment);
    }

    private Map<Long, String> buildAnonMap(List<BoardComment> allComments, Long postAuthorId) {
        Map<Long, String> anonMap = new LinkedHashMap<>();
        int counter = 1;
        for (BoardComment comment : allComments) {
            Long authorId = comment.getAuthor().getId();
            if (anonMap.containsKey(authorId)) continue;
            if (authorId.equals(postAuthorId)) {
                anonMap.put(authorId, "익명");
            } else {
                anonMap.put(authorId, "익명" + counter++);
            }
        }
        return anonMap;
    }

    private BoardCommentResponse toResponse(BoardComment comment, Map<Long, String> anonMap,
                                             Map<Long, List<BoardComment>> repliesByParentId) {
        List<BoardCommentResponse> replies = repliesByParentId
                .getOrDefault(comment.getId(), List.of())
                .stream()
                .map(reply -> new BoardCommentResponse(reply, anonMap.get(reply.getAuthor().getId()), List.of()))
                .collect(Collectors.toList());

        return new BoardCommentResponse(comment, anonMap.get(comment.getAuthor().getId()), replies);
    }

    private BoardPost getPostOrThrow(Long postId) {
        return boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
    }

    private BoardComment getCommentOrThrow(Long commentId) {
        return boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));
    }

    private void validateCommentAuthor(BoardComment comment, Long userId) {
        if (userId == null || !userId.equals(comment.getAuthor().getId())) {
            throw new AccessDeniedException("댓글 수정/삭제 권한이 없습니다.");
        }
    }
}
