package com.community.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Post 생성 요청 DTO")
@Getter
@Setter
public class PostCreateRequest {

    @Schema(description = "제목", example = "제목")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 26, message = "제목은 26자 이하여야 합니다.")
    private String title;

    @Schema(description = "본문", example = "본문")
    @NotBlank(message = "본문은 필수입니다.")
    private String body;

    @Schema(description = "이미지 파일")
    @NotNull(message = "이미지 파일은 필수입니다.")
    private MultipartFile file;
}
