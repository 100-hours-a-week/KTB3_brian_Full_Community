package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "댓글 Id 만 반환하는 응답 DTO")
@Getter
@AllArgsConstructor
public class CommentIdResponse {

    private final Long id;
}
