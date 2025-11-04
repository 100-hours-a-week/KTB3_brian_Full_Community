package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "댓글 리스트 응답 DTO")
@Getter
@AllArgsConstructor
public class CommentListResponse {

    private final List<CommentSingleResponse> commentList;
}
