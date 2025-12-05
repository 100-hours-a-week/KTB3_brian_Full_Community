package com.community.domain.board.service;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.dto.request.CommentRequest;
import com.community.domain.board.dto.response.CommentIdResponse;
import com.community.domain.board.dto.response.CommentSingleResponse;
import com.community.domain.board.model.Comment;
import com.community.domain.board.model.Post;
import com.community.domain.board.repository.CommentRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private Long authorId;
    private Long commenterId;
    private Long postId;

    @BeforeEach
    void setUp() {
        authorId = saveUser(1);
        commenterId = saveUser(2);
        postId = savePost(authorId, "t", "b");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("댓글을 생성하면 저장되고 CommentId 를 반환한다.")
    void createComment_and_response() {
        //given
        CommentRequest request = commentRequest("첫 댓글");

        //when
        CommentIdResponse response = commentService.createComment(postId, commenterId, request);

        //then
        Comment saved = commentRepository.findById(response.getId()).orElseThrow();
        assertEquals(request.getBody(), saved.getBody());
        assertEquals(postId, saved.getPost().getId());
        assertEquals(commenterId, saved.getUser().getId());
    }

    @Test
    @DisplayName("게시글 댓글 목록을 조회하면 PaginationRequest 조건에 맞는 PageResponse 를 반환한다.")
    void getComments_and_response() {
        //given
        Long anotherUserId = saveUser(3);
        createComment(postId, commenterId, "body-1");
        createComment(postId, anotherUserId, "body-2");

        PaginationRequest request = new PaginationRequest(0, 10, null, null);

        //when
        PageResponse<CommentSingleResponse> response = commentService.getComments(postId, request);

        //then
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        List<CommentSingleResponse> items = response.items();
        assertEquals(2, items.size());
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 댓글을 조회하면 POST_NOT_FOUND 예외를 던진다.")
    void getComments_throws_when_post_not_found() {
        //given
        PaginationRequest request = new PaginationRequest(0, 5, "id", PaginationRequest.SortDirection.ASC);

        //when
        CustomException ex = assertThrows(CustomException.class, () -> commentService.getComments(999L, request));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정을 호출하면 본문이 변경된다.")
    void updateComment() {
        //given
        Long commentId = createComment(postId, commenterId, "original");

        //when
        authenticate(commenterId);
        CommentRequest request = commentRequest("updated body");
        commentService.updateComment(postId, commentId, commenterId, request);

        //then
        Comment updated = commentRepository.findById(commentId).orElseThrow();
        assertEquals("updated body", updated.getBody());
    }

    @Test
    @DisplayName("댓글 삭제를 호출하면 해당 댓글이 제거된다.")
    void deleteComment() {
        //given
        Long commentId = createComment(postId, commenterId, "to delete");

        //when
        authenticate(commenterId);
        commentService.deleteComment(postId, commentId, commenterId);

        //then
        assertTrue(commentRepository.findById(commentId).isEmpty());
    }

    @Test
    @DisplayName("게시글 댓글 수를 집계할 수 있다.")
    void countComments_returnsTotal() {
        //given
        createComment(postId, commenterId, "first");
        createComment(postId, commenterId, "second");

        //when
        Long count = commentService.countComments(postId);

        //then
        assertEquals(2L, count);
    }

    private CommentRequest commentRequest(String body) {
        CommentRequest request = new CommentRequest();
        request.setBody(body);
        return request;
    }

    private Long createComment(Long postId, Long userId, String body) {
        Comment comment = new Comment(
                postRepository.findById(postId).orElseThrow(),
                userRepository.findById(userId).orElseThrow(),
                body
        );
        return commentRepository.save(comment);
    }

    private Long savePost(Long userId, String title, String body) {
        User author = userRepository.findById(userId).orElseThrow();
        Post post = new Post(author, title, "http://localhost:8080/files/default", body);
        return postRepository.save(post);
    }

    private Long saveUser(int sequence) {
        User user = new User(
                "e" + sequence + "@email.com",
                "Password1!",
                "nick" + sequence,
                "imageUrl"
        );
        userRepository.save(user);
        return user.getId();
    }

    private void authenticate(Long userId) {
        TestingAuthenticationToken token = new TestingAuthenticationToken(new AuthenticatedUser(userId), null, "test");
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
