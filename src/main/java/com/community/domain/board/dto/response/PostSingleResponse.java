package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Post 단일 응답 DTO")
@Getter
@AllArgsConstructor
public class PostSingleResponse {

    private final PostContent post;
    private final AuthorResponse author;
}
