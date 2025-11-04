package com.community.domain.board.dto.response;

import com.community.domain.board.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "Post 내용 응답 DTO")
@Getter
@AllArgsConstructor
public class PostContent {

    private final Long id;
    private final String title;
    private final String image;
    private final String body;
    private final Long viewCount;
    private final Long likeCount;
    private final Long commentCount;
    private final LocalDateTime createdAt;

    public static PostContent from(Post post, Long likeCount, Long commentCount) {
        return new PostContent(
                post.getId(),
                post.getTitle(),
                post.getImageUrl(),
                post.getBody(),
                post.getViewCount(),
                likeCount,
                commentCount,
                post.getCreatedAt()
        );
    }
}
