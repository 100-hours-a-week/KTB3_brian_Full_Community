package com.community.domain.board.repository;

import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
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

import static com.community.helper.PostMaker.getNumberedPost;
import static com.community.helper.UserMaker.getNumberedUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaPostLikeRepository.class, JpaUserRepository.class, JpaPostRepository.class})
public class PostLikeRepositoryTest {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("좋아요를 저장하면 게시글 id 와 회원 id 를 기준으로 조회할 수 있다.")
    void save_and_findByPostIdAndUserId() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        PostLike postLike = savePostLike(post, author);
        flushAndClear();

        //when
        PostLike found = postLikeRepository.findByPostIdAndUserId(post.getId(), author.getId()).orElseThrow();

        //then
        assertEquals(postLike, found);
    }

    @Test
    @DisplayName("좋아요 조회 결과가 없으면 empty Optional 을 반환한다.")
    void findByPostIdAndUserId_notFound() {
        assertTrue(postLikeRepository.findByPostIdAndUserId(1L, 1L).isEmpty());
    }

    @Test
    @DisplayName("게시글 id 와 회원 id 로 좋아요 존재 여부를 체크할 수 있다.")
    void existsByPostIdAndUserId() {
        //given
        User author = saveUser(1);
        User liker = saveUser(2);
        Post post = savePost(author, 1);
        savePostLike(post, liker);
        flushAndClear();

        //when & then
        assertTrue(postLikeRepository.existsByPostIdAndUserId(post.getId(), liker.getId()));
        assertFalse(postLikeRepository.existsByPostIdAndUserId(post.getId(), author.getId()));
    }

    @Test
    @DisplayName("저장된 좋아요를 삭제하면 더 이상 조회할 수 없다.")
    void delete() {
        //given
        User author = saveUser(1);
        User liker = saveUser(2);
        Post post = savePost(author, 1);
        savePostLike(post, liker);
        flushAndClear();

        //when
        PostLike stored = postLikeRepository.findByPostIdAndUserId(post.getId(), liker.getId()).orElseThrow();
        postLikeRepository.delete(stored);
        flushAndClear();

        //then
        assertTrue(postLikeRepository.findByPostIdAndUserId(post.getId(), liker.getId()).isEmpty());
    }

    @Test
    @DisplayName("게시글 id 로 좋아요를 일괄 삭제할 수 있다.")
    void deleteAllByPostId() {
        //given
        User author = saveUser(1);
        User liker = saveUser(2);
        Post post = savePost(author, 1);
        Post otherPost = savePost(author, 2);

        savePostLike(post, liker);
        savePostLike(post, author);
        savePostLike(otherPost, liker);
        flushAndClear();

        //when
        postLikeRepository.deleteAllByPostId(post.getId());
        flushAndClear();

        //then
        assertEquals(0L, postLikeRepository.countByPostId(post.getId()));
        assertEquals(1L, postLikeRepository.countByPostId(otherPost.getId()));
    }

    @Test
    @DisplayName("게시글 id 로 좋아요 수를 집계할 수 있다.")
    void countByPostId() {
        //given
        User author = saveUser(1);
        User liker = saveUser(2);
        Post post = savePost(author, 1);
        savePostLike(post, liker);
        savePostLike(post, author);
        flushAndClear();

        //when
        Long count = postLikeRepository.countByPostId(post.getId());

        //then
        assertEquals(2L, count);
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

    private PostLike savePostLike(Post post, User user) {
        PostLike postLike = new PostLike(post, user);
        postLikeRepository.save(postLike);
        return postLike;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
