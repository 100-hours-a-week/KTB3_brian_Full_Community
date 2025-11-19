package com.community.domain.user.dto.request;

import com.community.global.validation.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.community.global.validation.MessageConstants.PASSWORD_REQUIRED;
import static com.community.global.validation.MessageConstants.PASSWORD_SIZE_INVALID;

@Schema(description = "비밀번호 업데이트 요청 DTO")
@Data
public class PasswordUpdateRequest {

    @Schema(description = "새로운 비밀번호", example = "A1234567890a!")
    @NotBlank(message = PASSWORD_REQUIRED)
    @Password
    @Size(min = 8, max = 20, message = PASSWORD_SIZE_INVALID)
    private String password;
}
