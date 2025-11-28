package com.community.domain.board.service;

import com.community.domain.board.dto.request.PostCreateRequest;
import com.community.domain.board.dto.request.PostUpdateRequest;
import com.community.domain.board.dto.response.PostIdResponse;
import com.community.domain.board.dto.response.PostLikeResponse;
import com.community.domain.board.dto.response.PostSingleResponse;
import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
import com.community.domain.board.repository.PostLikeRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.file.service.FileStorageService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private CommentService commentService;
    @Mock
    private PostViewEventService postViewEventService;

    @InjectMocks
    private PostService postService;


    @Test
    @DisplayName("게시글 목록 조회시 PaginationRequest 를 기반으로 PageResponse 를 반환한다.")
    void getPostList_and_response() {
        // given
        PaginationRequest request = new PaginationRequest(0, 10, null, null);

        User user1 = user(1L);
        User user2 = user(2L);

        Post post1 = post(1L, user1);
        Post post2 = post(2L, user2);

        PageResult<Post> pageResult = new PageResult<>(
                List.of(post1, post2),
                2L,
                1
        );
        when(postRepository.findAll(request)).thenReturn(pageResult);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        // when
        PageResponse<PostSingleResponse> response = postService.getPostList(request);

        // then
        verify(postRepository, times(1)).findAll(request);

        assertThat(response.items()).hasSize(2);
        PostSingleResponse item1 = response.items().get(0);
        assertThat(item1.getPost().getId()).isEqualTo(post1.getId());
        assertThat(item1.getPost().getTitle()).isEqualTo(post1.getTitle());

        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(request.page());
        assertThat(response.size()).isEqualTo(request.size());
    }

    @Test
    @DisplayName("작성자 ID 기준 게시글 목록 조회시 PaginationRequest 를 기반으로 사용자가 작성한 게시글의 PageResponse 를 반환한다.")
    void getPostsByUserId_and_response() {
        // given
        PaginationRequest request = new PaginationRequest(0, 10, null, null);

        User user1 = user(1L);
        User user2 = user(2L);

        Post post1 = post(1L, user1);
        Post post2 = post(2L, user1);
        Post post3 = post(3L, user2);

        PageResult<Post> pageResult = new PageResult<>(
                List.of(post1, post2),
                2L,
                1
        );
        when(postRepository.findByUserId(user1.getId(), request)).thenReturn(pageResult);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // when
        PageResponse<PostSingleResponse> response = postService.getPostsByUserId(request, user1.getId());

        // then
        verify(postRepository, times(1)).findByUserId(user1.getId(),request);

        assertThat(response.items()).hasSize(2);
        PostSingleResponse item1 = response.items().get(0);
        assertThat(item1.getPost().getId()).isEqualTo(post1.getId());
        assertThat(item1.getPost().getTitle()).isEqualTo(post1.getTitle());

        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(request.page());
        assertThat(response.size()).isEqualTo(request.size());
    }

    @Test
    @DisplayName("좋아요한 게시글 목록 조회시 PaginationRequest 를 기반으로 PageResponse 를 반환한다.")
    void getPostsByPostLikeUserId_and_response() {
        // given
        PaginationRequest request = new PaginationRequest(0, 10, null, null);
        long likerId = 99L;

        User author1 = user(1L);
        User author2 = user(2L);
        Post post1 = post(1L, author1);
        Post post2 = post(2L, author2);

        PageResult<Post> pageResult = new PageResult<>(
                List.of(post1, post2),
                2L,
                1
        );

        when(postRepository.findByPostLikeUserId(likerId, request)).thenReturn(pageResult);
        when(userRepository.findById(author1.getId())).thenReturn(Optional.of(author1));
        when(userRepository.findById(author2.getId())).thenReturn(Optional.of(author2));
        when(postLikeRepository.countByPostId(post1.getId())).thenReturn(3L);
        when(postLikeRepository.countByPostId(post2.getId())).thenReturn(5L);
        when(commentService.countComments(post1.getId())).thenReturn(1L);
        when(commentService.countComments(post2.getId())).thenReturn(2L);

        // when
        PageResponse<PostSingleResponse> response = postService.getPostsByPostLikeUserId(request, likerId);

        // then
        verify(postRepository).findByPostLikeUserId(likerId, request);
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).getPost().getId()).isEqualTo(post1.getId());
        assertThat(response.items().get(1).getPost().getId()).isEqualTo(post2.getId());
        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(request.page());
        assertThat(response.size()).isEqualTo(request.size());
    }


    @Test
    @DisplayName("게시글을 상세 조회하면 조회수를 증가시키는 이벤트를 추가하고 게시글을 반환한다.")
    void viewPost_and_response() {
        User author = user(9L);
        Post post = post(4L, author);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPostId(post.getId())).thenReturn(5L);
        when(commentService.countComments(post.getId())).thenReturn(7L);
        when(userRepository.findById(author.getId())).thenReturn(Optional.of(author));

        PostSingleResponse response = postService.viewPost(post.getId());

        assertThat(response.getPost().getId()).isEqualTo(post.getId());
        verify(postViewEventService).addEvent(post.getId());
    }

    @Test
    @DisplayName("게시글을 생성하면 파일을 저장하고 PostId 를 반환한다.")
    void createPost() {
        long userId = 15L;
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "png".getBytes());
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("title");
        request.setBody("body");
        request.setFile(file);

        User user = user(userId);
        when(fileStorageService.save(file)).thenReturn("stored-url");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(55L);

        PostIdResponse response = postService.createPost(userId, request);

        assertThat(response.getId()).isEqualTo(55L);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTitle()).isEqualTo("title");
        assertThat(saved.getBody()).isEqualTo("body");
        assertThat(saved.getImageUrl()).isEqualTo("stored-url");
    }

    @Test
    @DisplayName("게시글 생성 시 회원이 존재하지 않으면 NOT_FOUND_USER 예외를 던진다.")
    void createPost_throws_when_user_not_found() {
        long userId = 99L;
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "png".getBytes());
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("title");
        request.setBody("body");
        request.setFile(file);

        when(fileStorageService.save(file)).thenReturn("stored-url");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> postService.createPost(userId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("게시글을 수정하면 제목, 본문, 이미지를 갱신하고 이전 이미지를 삭제한다.")
    void updatePost() {
        User user = user(7L);
        Post post = post(21L, user);
        String previousImage = post.getImageUrl();
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("new title");
        request.setBody("new body");
        MockMultipartFile newImage = new MockMultipartFile("file", "new.png", "image/png", "data".getBytes());
        request.setFile(newImage);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(fileStorageService.save(newImage)).thenReturn("new-image");

        PostIdResponse response = postService.updatePost(post.getId(), user.getId(), request);

        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(post.getTitle()).isEqualTo("new title");
        assertThat(post.getBody()).isEqualTo("new body");
        assertThat(post.getImageUrl()).isEqualTo("new-image");
        verify(fileStorageService).delete(previousImage);
    }

    @Test
    @DisplayName("게시글 수정 시 이미지가 요청에 포함되어 있지 않으면 이미지는 갱신하지 않는다.")
    void updatePost_skips_image_when_not_include_in_request() {
        User user = user(7L);
        Post post = post(21L, user);
        String originImageUrl = post.getImageUrl();
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("new title");
        request.setBody("new body");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        PostIdResponse response = postService.updatePost(post.getId(), user.getId(), request);

        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(post.getTitle()).isEqualTo("new title");
        assertThat(post.getBody()).isEqualTo("new body");
        assertThat(post.getImageUrl()).isEqualTo(originImageUrl);
        verify(fileStorageService, never()).save(any());
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("게시글을 삭제하면 파일을 함께 정리한다.")
    void deletePost() {
        User user = user(3L);
        Post post = post(8L, user);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        postService.deletePost(post.getId(), user.getId());

        verify(fileStorageService).delete(post.getImageUrl());
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("좋아요가 없으면 새로 저장하고 true 를 반환한다.")
    void toggleLike_save_when_not_liked() {
        long postId = 11L;
        long userId = 12L;
        User user = user(userId);
        Post post = post(postId, user);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());

        PostLikeResponse response = postService.toggleLike(postId, userId);

        assertTrue(response.isLiked());
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    @DisplayName("이미 좋아요가 있으면 삭제하고 false 를 반환한다.")
    void toggleLike_delete_when_already_liked() {
        long postId = 11L;
        long userId = 12L;
        User user = user(userId);
        Post post = post(postId, user);
        PostLike existing = new PostLike(post, user);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.of(existing));

        PostLikeResponse response = postService.toggleLike(postId, userId);

        assertFalse(response.isLiked());
        verify(postLikeRepository).delete(existing);
    }

    @Test
    @DisplayName("회원이 게시글을 좋아요 했는지 여부를 확인한다.")
    void checkUserLikedPost() {
        long postId = 2L;
        long userId = 3L;
        User user = user(userId);
        Post post = post(postId, user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(true);

        PostLikeResponse response = postService.checkUserLikedPost(postId, userId);

        assertTrue(response.isLiked());
        verify(postLikeRepository).existsByPostIdAndUserId(postId, userId);
    }

    private User user(Long id) {
        User user = new User("user" + id + "@email.com", "password", "nick" + id, "image" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post post(Long id, User user) {
        Post post = new Post(user, "title " + id, "image-" + id, "body " + id);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
