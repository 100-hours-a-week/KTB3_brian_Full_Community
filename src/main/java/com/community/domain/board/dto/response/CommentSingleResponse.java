package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "댓글 단일 응답 DTO")
@Getter
@AllArgsConstructor
public class CommentSingleResponse {

    private final CommentContent comment;
    private final AuthorResponse author;
}
