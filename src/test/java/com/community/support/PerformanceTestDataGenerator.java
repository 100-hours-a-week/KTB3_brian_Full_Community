package com.community.support;

import com.community.domain.board.model.Comment;
import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
import com.community.domain.board.model.PostViewEvent;
import com.community.domain.board.repository.CommentRepository;
import com.community.domain.board.repository.JpaPostViewEventRepository;
import com.community.domain.board.repository.PostLikeRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceTestDataGenerator {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final JpaPostViewEventRepository postViewEventRepository;
    private final UserService userService;

    @Transactional
    public TestIds generate(
            int userCount,
            int postsPerUser,
            int commentsPerPost,
            int likesPerPost,
            int viewsPerPost
    ) {
        List<User> users = new ArrayList<>(userCount);

        // 1) 유저 생성
        for (int i = 0; i < userCount; i++) {
            User user = new User(
                    "user" + i + "@example.com",
                    "encoded-password",
                    "user" + i,
                    "http://localhost:8080/file/" + i
            );
            users.add(user);
        }

        users.forEach(userRepository::save);

        // 삭제 실험에 쓸 "특정 유저" 하나 선택 (0번 유저 기준)
        User targetUser = users.get(0);

        List<Post> allPosts = new ArrayList<>(userCount * postsPerUser);
        List<Comment> allComments = new ArrayList<>();
        List<PostLike> allLikes = new ArrayList<>();
        List<PostViewEvent> allViewEvents = new ArrayList<>();

        // 2) 각 유저에 대해 게시글/댓글/좋아요/뷰 이벤트 생성
        for (int u = 0; u < userCount; u++) {
            User author = users.get(u);

            for (int p = 0; p < postsPerUser; p++) {
                Post post = new Post(
                        author,
                        "Post " + p + " by user " + u,
                        "This is a test post body. user=" + u + ", post=" + p,
                        "http://localhost:8080/img/" + u + "_" + p + ".png"
                );
                allPosts.add(post);
            }
        }
        allPosts.forEach(postRepository::save);

        Post targetPost = allPosts.stream()
                .filter(p -> p.getUser().getId().equals(targetUser.getId()))
                .findFirst()
                .orElseThrow();

        // 3) 댓글 / 좋아요 / 뷰 이벤트 생성
        for (Post post : allPosts) {
            // 댓글
            for (int c = 0; c < commentsPerPost; c++) {
                Comment comment = new Comment(
                        post,
                        targetUser,
                        "Comment " + c + " on post " + post.getId()
                );
                allComments.add(comment);
            }

            // 좋아요
            for (int l = 0; l < likesPerPost; l++) {
                User liker = users.get(l % userCount); // 여러 유저가 섞어서 좋아요 누른다고 가정
                PostLike like = new PostLike(
                        post,
                        liker
                );

                allLikes.add(like);
            }

            // 조회 이벤트
            for (int v = 0; v < viewsPerPost; v++) {
                PostViewEvent viewEvent = new PostViewEvent(
                        post.getId()
                );

                allViewEvents.add(viewEvent);
            }
        }
        allComments.forEach(commentRepository::save);
        allLikes.forEach(postLikeRepository::save);
        allViewEvents.forEach(postViewEventRepository::save);

        return new TestIds(targetUser.getId(), targetPost.getId());
    }

    public record TestIds(Long userId, Long postId) {
    }
}
