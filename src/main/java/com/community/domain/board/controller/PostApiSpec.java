package com.community.domain.board.controller;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.dto.request.PostCreateRequest;
import com.community.domain.board.dto.request.PostUpdateRequest;
import com.community.domain.board.dto.response.PostIdResponse;
import com.community.domain.board.dto.response.PostLikeResponse;
import com.community.domain.board.dto.response.PostSingleResponse;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PaginationRequest;
import com.community.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Post", description = "게시글 관리 API")
public interface PostApiSpec {

    @Operation(summary = "게시글 리스트 조회", description = "페이징 기능을 바탕으로 게시글 리스트를 조회합니다.")
    ResponseEntity<ApiResponse<PageResponse<PostSingleResponse>>> getPosts(PaginationRequest paginationRequest);

    @Operation(summary = "게시글 생성", description = "게시글을 작성하여 게시판에 등록합니다.")
    ResponseEntity<ApiResponse<PostIdResponse>> createPost(AuthenticatedUser authenticatedUser,
                                                           PostCreateRequest request);

    @Operation(summary = "게시글 단건 조회", description = "게시글을 단건 조회합니다. 조회수가 증가합니다.")
    ResponseEntity<ApiResponse<PostSingleResponse>> getPost(Long postId);

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    ResponseEntity<ApiResponse<PostIdResponse>> updatePost(Long postId,
                                                           AuthenticatedUser authenticatedUser,
                                                           PostUpdateRequest request);

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 관련된 댓글도 함께 삭제됩니다.")
    ResponseEntity<ApiResponse<Void>> deletePost(Long postId,
                                                 AuthenticatedUser authenticatedUser);

    @Operation(summary = "게시글 좋아요 토글", description = "게시글의 좋아요를 토글로 표현합니다.")
    ResponseEntity<ApiResponse<Void>> toggleLike(Long postId,
                                                 AuthenticatedUser authenticatedUser);

    @Operation(summary = "게시글 좋아요 여부 조회", description = "현재 회원이 특정 게시글에 좋아요를 누른 상태인지 조회합니다.")
    ResponseEntity<ApiResponse<PostLikeResponse>> isUserLikedPost(Long postId,
                                                                  AuthenticatedUser authenticatedUser);
}
