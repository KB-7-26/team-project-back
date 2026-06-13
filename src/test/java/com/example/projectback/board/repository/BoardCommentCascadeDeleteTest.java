package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardComment;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BoardCommentCascadeDeleteTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BoardCommentRepository boardCommentRepository;

    @Test
    @DisplayName("부모 댓글 삭제 시 대댓글이 연쇄 삭제된다")
    void deleteParentComment_cascadesDeleteToReplies() {
        // given
        User author = User.builder()
                .firebaseUid("uid-cascade-test")
                .name("테스트유저")
                .nickname("익명테스터")
                .email("cascade@test.com")
                .cohort("1기")
                .gender("M")
                .build();
        em.persist(author);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("테스트 게시글")
                .content("내용")
                .build();
        em.persist(post);

        BoardComment parent = BoardComment.builder()
                .post(post)
                .author(author)
                .content("부모 댓글")
                .build();
        em.persist(parent);

        BoardComment reply = BoardComment.builder()
                .post(post)
                .author(author)
                .parentComment(parent)
                .content("대댓글")
                .build();
        em.persist(reply);

        em.flush();
        em.clear(); // 영속성 컨텍스트 초기화 → 이후 조회는 DB에서 읽음

        Long parentId = parent.getId();
        Long replyId = reply.getId();

        // when - DB에서 재로드 후 삭제 (Hibernate가 lazy replies 컬렉션을 SELECT 후 cascade DELETE)
        BoardComment loadedParent = boardCommentRepository.findById(parentId).orElseThrow();
        boardCommentRepository.delete(loadedParent);
        boardCommentRepository.flush();
        em.clear();

        // then - 대댓글도 삭제되었는지 확인
        assertThat(boardCommentRepository.findById(replyId)).isEmpty();
    }

    @Test
    @DisplayName("대댓글만 단독 삭제 시 부모 댓글은 유지된다")
    void deleteReply_doesNotDeleteParent() {
        // given
        User author = User.builder()
                .firebaseUid("uid-reply-delete-test")
                .name("테스트유저2")
                .nickname("익명테스터2")
                .email("reply@test.com")
                .cohort("1기")
                .gender("F")
                .build();
        em.persist(author);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("게시글2")
                .content("내용2")
                .build();
        em.persist(post);

        BoardComment parent = BoardComment.builder()
                .post(post)
                .author(author)
                .content("부모 댓글")
                .build();
        em.persist(parent);

        BoardComment reply = BoardComment.builder()
                .post(post)
                .author(author)
                .parentComment(parent)
                .content("대댓글")
                .build();
        em.persist(reply);

        em.flush();
        em.clear();

        Long parentId = parent.getId();
        Long replyId = reply.getId();

        // when
        boardCommentRepository.deleteById(replyId);
        boardCommentRepository.flush();
        em.clear();

        // then
        assertThat(boardCommentRepository.findById(replyId)).isEmpty();
        assertThat(boardCommentRepository.findById(parentId)).isPresent();
    }
}