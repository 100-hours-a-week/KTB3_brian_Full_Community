package com.community.domain.board.service;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.dto.request.CommentRequest;
import com.community.domain.board.dto.request.PostCreateRequest;
import com.community.domain.board.dto.response.PostIdResponse;
import com.community.domain.board.dto.response.PostLikeResponse;
import com.community.domain.board.model.Post;
import com.community.domain.board.repository.CommentRepository;
import com.community.domain.board.repository.PostLikeRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager em;

    private Long authorId;
    private Long likerId;
    private Long commenterId;
    private Long preparedPostId;
    private Long freshPostId;

    @BeforeEach
    void setUp() {
        authorId = saveUser(1);
        likerId = saveUser(2);
        commenterId = saveUser(3);

        preparedPostId = postService.createPost(authorId, createRequest("seed title", "seed body")).getId();
        freshPostId = postService.createPost(authorId, createRequest("fresh title", "fresh body")).getId();

        postService.toggleLike(preparedPostId, likerId);
        CommentRequest seedComment = new CommentRequest();
        seedComment.setBody("seed comment");
        commentService.createComment(preparedPostId, commenterId, seedComment);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("게시글 생성 시 파일이 저장되고 Post 가 영속화된다.")
    void createPost_persists_post_and_file() {
        PostCreateRequest request = createRequest("integration title", "body");

        PostIdResponse response = postService.createPost(authorId, request);

        Post saved = postRepository.findById(response.getId()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("integration title");
        assertThat(saved.getBody()).isEqualTo("body");
        assertThat(saved.getImageUrl()).startsWith("http://localhost:8080/files/");
        assertThat(saved.getUser().getId()).isEqualTo(authorId);
    }

    @Test
    @DisplayName("좋아요 토글 흐름 통합 테스트 - 생성 후 삭제까지 처리한다.")
    void toggleLike_flow() {
        assertThat(postLikeRepository.countByPostId(freshPostId)).isZero();

        PostLikeResponse liked = postService.toggleLike(freshPostId, likerId);
        assertThat(liked.isLiked()).isTrue();
        assertThat(postLikeRepository.countByPostId(freshPostId)).isEqualTo(1L);

        PostLikeResponse unliked = postService.toggleLike(freshPostId, likerId);
        assertThat(unliked.isLiked()).isFalse();
        assertThat(postLikeRepository.countByPostId(freshPostId)).isZero();
    }

    @Test
    @DisplayName("게시글 삭제 시 연관된 좋아요와 댓글이 정리된다.")
    void deletePost_removes_associations() {
        //given
        authenticate(authorId);
        assertThat(postLikeRepository.countByPostId(preparedPostId)).isEqualTo(1L);
        assertThat(commentRepository.countByPostId(preparedPostId)).isEqualTo(1L);

        //when
        postService.deletePost(preparedPostId, authorId);
        em.flush();
        em.clear();

        //then
        assertThat(postRepository.findById(preparedPostId)).isEmpty();
        assertThat(postLikeRepository.countByPostId(preparedPostId)).isZero();
        assertThat(commentRepository.countByPostId(preparedPostId)).isZero();
    }

    private PostCreateRequest createRequest(String title, String body) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "image-bytes".getBytes()
        );
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setFile(file);
        return request;
    }

    private void authenticate(Long userId) {
        TestingAuthenticationToken token = new TestingAuthenticationToken(new AuthenticatedUser(userId), null, "test");
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private Long saveUser(int number) {
        User user = new User("email" + number + "@email.com", "Password1!", "nick-" + number, "http://localhost:8080/file/default");
        userRepository.save(user);
        return user.getId();
    }
}
