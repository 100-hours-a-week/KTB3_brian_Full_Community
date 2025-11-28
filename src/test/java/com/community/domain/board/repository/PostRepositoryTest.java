package com.community.domain.board.repository;

import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
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

import static com.community.helper.PostMaker.*;
import static com.community.helper.UserMaker.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaPostRepository.class, JpaUserRepository.class})
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("새로운 게시글을 저장하는 경우 id 를 할당받게 되고, id 를 기준으로 조회할 수 있다.")
    void save() {
        //given
        User u = getNumberedUser(1);
        userRepository.save(u);

        Post p = getNumberedPost(u, 1);
        assertNull(p.getId());

        //when
        Long id = postRepository.save(p);
        em.flush();
        em.clear();
        Optional<Post> found = postRepository.findById(id);

        //then
        assertTrue(found.isPresent());
        assertNotNull(id);
        assertEquals(p, found.get());
    }

    @Test
    @DisplayName("조회 결과가 없으면 empty 인 Optional 을 반환한다.")
    void findById_NotFound_ReturnsEmpty() {
        assertTrue(postRepository.findById(1L).isEmpty());
    }

    @Test
    @DisplayName("엔티티를 삭제하면 더 이상 조회가 불가능하다.")
    void save_and_delete() {
        //given
        User u = getNumberedUser(1);
        userRepository.save(u);
        Post p = getNumberedPost(u, 1);
        postRepository.save(p);

        em.flush();
        em.clear();

        //when
        Post post = postRepository.findById(p.getId()).get();
        postRepository.delete(post);

        //then
        assertTrue(postRepository.findById(p.getId()).isEmpty());
    }

    @Test
    @DisplayName("페이지 조건에 맞춰 게시글 목록을 조회할 수 있다.")
    void findAll() {
        //given
        User author = saveUser(1);
        Post p1 = savePost(author, 1);
        Post p2 = savePost(author, 2);
        savePost(author, 3);
        flushAndClear();

        PaginationRequest request = new PaginationRequest(0, 2, "title", PaginationRequest.SortDirection.ASC);

        //when
        PageResult<Post> result = postRepository.findAll(request);

        //then
        assertEquals(2, result.items().size());
        assertEquals(3, result.totalElements());
        assertEquals(2, result.totalPages());
        List<Long> itemIds = result.items().stream()
                .map(Post::getId)
                .toList();
        assertEquals(List.of(p1.getId(), p2.getId()), itemIds);
    }

    @Test
    @DisplayName("특정 사용자의 게시글만 페이징으로 조회할 수 있다.")
    void findByUserId() {
        //given
        User author = saveUser(1);
        User another = saveUser(2);
        savePost(author, 1);
        savePost(author, 2);
        savePost(author, 3);
        savePost(another, 11);
        flushAndClear();

        PaginationRequest request = new PaginationRequest(0, 10, "createdAt", PaginationRequest.SortDirection.DESC);

        //when
        PageResult<Post> result = postRepository.findByUserId(author.getId(), request);

        //then
        assertEquals(3, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(3, result.items().size());
        assertTrue(result.items().stream().allMatch(post -> post.getUser().getId().equals(author.getId())));
    }

    @Test
    @DisplayName("좋아요를 누른 게시글만 페이징으로 조회할 수 있다.")
    void findByPostLikeUserId() {
        //given
        User author = saveUser(1);
        User liker = saveUser(2);
        User otherLiker = saveUser(3);

        Post liked1 = savePost(author, 1);
        Post liked2 = savePost(author, 2);
        Post notLiked = savePost(author, 3);

        persistPostLike(liker, liked1);
        persistPostLike(liker, liked2);
        persistPostLike(otherLiker, notLiked);
        flushAndClear();

        PaginationRequest request = new PaginationRequest(0, 10, "createdAt", PaginationRequest.SortDirection.DESC);

        //when
        PageResult<Post> result = postRepository.findByPostLikeUserId(liker.getId(), request);

        //then
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        List<Long> ids = result.items().stream().map(Post::getId).toList();
        assertTrue(ids.containsAll(List.of(liked1.getId(), liked2.getId())));
        assertFalse(ids.contains(notLiked.getId()));
    }

    @Test
    @DisplayName("특정 사용자의 모든 게시글을 조회할 수 있다.")
    void findAllByUserId() {
        //given
        User author = saveUser(1);
        User other = saveUser(2);

        Post first = savePost(author, 1);
        Post second = savePost(author, 2);
        savePost(other, 3);
        flushAndClear();

        //when
        List<Post> posts = postRepository.findAllByUserId(author.getId());

        //then
        assertEquals(2, posts.size());
        assertTrue(posts.stream().allMatch(post -> post.getUser().getId().equals(author.getId())));
        List<Long> ids = posts.stream().map(Post::getId).toList();
        assertTrue(ids.containsAll(List.of(first.getId(), second.getId())));
    }

    @Test
    @DisplayName("조회 수 증가 요청이 양수일 때만 값을 변경한다.")
    void increaseViewCount() {
        //given
        User author = saveUser(1);
        Post post = savePost(author, 1);
        flushAndClear();

        //when
        postRepository.increaseViewCount(post.getId(), 5);
        postRepository.increaseViewCount(post.getId(), 0);
        postRepository.increaseViewCount(post.getId(), -3);
        flushAndClear();

        //then
        Post updated = postRepository.findById(post.getId()).orElseThrow();
        assertEquals(5, updated.getViewCount());
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

    private void persistPostLike(User liker, Post post) {
        em.persist(new PostLike(post, liker));
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
