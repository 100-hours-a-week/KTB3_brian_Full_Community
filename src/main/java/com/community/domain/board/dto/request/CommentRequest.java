package com.community.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "댓글 관련 요청 DTO")
@Data
public class CommentRequest {

    @Schema(description = "본문", example = "본문")
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String body;
}
