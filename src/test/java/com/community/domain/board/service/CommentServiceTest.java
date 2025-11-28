package com.community.domain.board.service;

import com.community.domain.board.dto.request.CommentRequest;
import com.community.domain.board.dto.response.CommentIdResponse;
import com.community.domain.board.dto.response.CommentSingleResponse;
import com.community.domain.board.model.Comment;
import com.community.domain.board.model.Post;
import com.community.domain.board.repository.CommentRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("게시글의 댓글 목록 조회시 PaginationRequest 를 기반으로 PageResponse 를 반환한다.")
    void getComments() {
        //given
        long postId = 1L;
        PaginationRequest request = new PaginationRequest(0, 10, null, null);
        Post post = post(postId);
        User author = user(10L);
        Comment comment = comment(5L, post, author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostId(postId, request))
                .thenReturn(new PageResult<>(List.of(comment), 1, 1));
        when(userRepository.findById(author.getId())).thenReturn(Optional.of(author));

        //when
        PageResponse<CommentSingleResponse> response = commentService.getComments(postId, request);

        //then
        assertThat(response.items()).hasSize(1);
        CommentSingleResponse item = response.items().get(0);
        assertThat(item.getComment().getId()).isEqualTo(comment.getId());
        assertThat(item.getAuthor().getId()).isEqualTo(author.getId());
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("게시글의 댓글 목록 조회시 게시글이 존재하지 않으면 POST_NOT_FOUND 예외를 던진다.")
    void getComments_throws_when_post_not_found() {
        //given
        long postId = 999L;
        PaginationRequest request = new PaginationRequest(0, 10, null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> commentService.getComments(postId, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글을 생성하면 CommentId 를 반환한다.")
    void createComment_and_response() {
        long postId = 1L;
        long userId = 2L;
        Post post = post(postId);
        User user = user(userId);
        CommentRequest request = new CommentRequest();
        request.setBody("hello");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(100L);

        CommentIdResponse response = commentService.createComment(postId, userId, request);

        assertThat(response.getId()).isEqualTo(100L);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getPost()).isEqualTo(post);
        assertThat(saved.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("댓글 생성 시 게시글이 없으면 POST_NOT_FOUND 예외를 던진다.")
    void createComment_throws_when_post_not_found() {
        CommentRequest request = new CommentRequest();
        request.setBody("body");
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> commentService.createComment(1L, 2L, request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글을 수정하면 내용이 변경되고 CommentId 를 반환한다.")
    void updateComment() {
        Post post = post(1L);
        User user = user(3L);
        Comment comment = comment(10L, post, user);
        CommentRequest request = new CommentRequest();
        request.setBody("updated");

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        CommentIdResponse response = commentService.updateComment(post.getId(), comment.getId(), user.getId(), request);

        assertThat(response.getId()).isEqualTo(comment.getId());
        assertThat(comment.getBody()).isEqualTo("updated");
    }

    @Test
    @DisplayName("댓글을 삭제하면 repository 에서 제거된다.")
    void deleteComment() {
        Post post = post(1L);
        User user = user(2L);
        Comment comment = comment(9L, post, user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(post.getId(), comment.getId(), user.getId());

        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("게시글 댓글 수를 조회할 수 있다.")
    void countComments_and_response() {
        when(commentRepository.countByPostId(1L)).thenReturn(5L);

        Long count = commentService.countComments(1L);

        assertThat(count).isEqualTo(5L);
        verify(commentRepository).countByPostId(1L);
    }

    private Post post(Long id) {
        User user = user(100L);
        Post post = new Post(user, "title" + id, "img" + id, "body" + id);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private User user(Long id) {
        User user = new User("user" + id + "@email.com", "password", "nick" + id, "image" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Comment comment(Long id, Post post, User user) {
        Comment comment = new Comment(post, user, "body-" + id);
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now());
        return comment;
    }
}
