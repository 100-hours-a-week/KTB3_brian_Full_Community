package com.community.domain.board.repository;

import com.community.domain.board.model.Comment;
import com.community.domain.board.model.Post;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.JpaUserRepository;
import com.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static com.community.helper.CommentMaker.getNumberedComment;
import static com.community.helper.PostMaker.getNumberedPost;
import static com.community.helper.UserMaker.getNumberedUser;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaCommentRepository.class, JpaPostRepository.class, JpaUserRepository.class})
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("댓글을 저장하면 id 를 기준으로 조회할 수 있다.")
    void save_and_findById() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        Comment comment = saveComment(post, author, 1);
        flushAndClear();

        //when
        Optional<Comment> found = commentRepository.findById(comment.getId());

        //then
        assertTrue(found.isPresent());
        assertEquals(comment, found.get());
    }

    @Test
    @DisplayName("댓글 조회 결과가 없으면 empty Optional 을 반환한다.")
    void findById_notFound() {
        assertTrue(commentRepository.findById(999L).isEmpty());
    }

    @Test
    @DisplayName("댓글을 삭제하면 더 이상 조회할 수 없다.")
    void delete() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        Comment comment = saveComment(post, author, 1);
        flushAndClear();

        //when
        Comment stored = commentRepository.findById(comment.getId()).orElseThrow();
        commentRepository.delete(stored);
        flushAndClear();

        //then
        assertTrue(commentRepository.findById(comment.getId()).isEmpty());
    }

    @Test
    @DisplayName("게시글 id 로 댓글을 페이징 조회할 수 있다.")
    void findByPostId() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        saveComment(post, author, 1);
        saveComment(post, author, 2);
        saveComment(post, author, 3);
        Post otherPost = savePost(author, 2);
        saveComment(otherPost, author, 4);
        flushAndClear();

        PaginationRequest request = new PaginationRequest(0, 2, "id", PaginationRequest.SortDirection.ASC);

        //when
        PageResult<Comment> result = commentRepository.findByPostId(post.getId(), request);

        //then
        assertEquals(3, result.totalElements());
        assertEquals(2, result.totalPages());
        assertEquals(2, result.items().size());
        assertTrue(result.items().stream().allMatch(c -> c.getPost().getId().equals(post.getId())));
    }

    @Test
    @DisplayName("게시글 id 로 댓글 수를 집계할 수 있다.")
    void countByPostId() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        saveComment(post, author, 1);
        saveComment(post, author, 2);
        Post otherPost = savePost(author, 2);
        saveComment(otherPost, author, 3);
        flushAndClear();

        //when
        Long count = commentRepository.countByPostId(post.getId());

        //then
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("게시글 id 로 댓글을 일괄 삭제할 수 있다.")
    void deleteByPostId() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        Post otherPost = savePost(author, 2);
        saveComment(post, author, 1);
        saveComment(post, author, 2);
        saveComment(otherPost, author, 3);
        flushAndClear();

        //when
        commentRepository.deleteByPostId(post.getId());
        flushAndClear();

        //then
        assertEquals(0, commentRepository.findByPostId(post.getId(), new PaginationRequest(0, 10, "id", PaginationRequest.SortDirection.ASC)).items().size());
        assertEquals(1, commentRepository.findByPostId(otherPost.getId(), new PaginationRequest(0, 10, "id", PaginationRequest.SortDirection.ASC)).totalElements());
    }

    @Test
    @DisplayName("사용자 id 로 해당 사용자의 댓글을 일괄 삭제할 수 있다.")
    void deleteByUserId() {
        //given
        User author = saveUser(1);
        User other = saveUser(2);
        Post post = savePost(author, 1);
        saveComment(post, author, 1);
        saveComment(post, author, 2);
        saveComment(post, other, 3);
        flushAndClear();

        //when
        commentRepository.deleteByUserId(author.getId());
        flushAndClear();

        //then
        List<Comment> remaining = commentRepository.findByPostId(post.getId(), new PaginationRequest(0, 10, "id", PaginationRequest.SortDirection.ASC)).items();
        assertEquals(1, remaining.size());
        assertEquals(other.getId(), remaining.get(0).getUser().getId());
    }

    private User saveUser(int sequence) {
        User user = getNumberedUser(sequence);
        userRepository.save(user);
        return user;
    }

    private Post savePost(User user, int sequence) {
        Post post = getNumberedPost(user, sequence);
        postRepository.save(post);
        return post;
    }

    private Comment saveComment(Post post, User user, int sequence) {
        Comment comment = getNumberedComment(post, user, sequence);
        commentRepository.save(comment);
        return comment;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
