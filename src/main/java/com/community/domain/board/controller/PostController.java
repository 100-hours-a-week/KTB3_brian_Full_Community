package com.community.domain.board.controller;

import com.community.domain.auth.annotation.Auth;
import com.community.domain.auth.annotation.AuthUser;
import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.common.util.UriUtil;
import com.community.domain.board.dto.request.PostCreateRequest;
import com.community.domain.board.dto.request.PostUpdateRequest;
import com.community.domain.board.dto.response.PostIdResponse;
import com.community.domain.board.dto.response.PostLikeResponse;
import com.community.domain.board.dto.response.PostListResponse;
import com.community.domain.board.dto.response.PostSingleResponse;
import com.community.domain.board.service.PostService;
import com.community.global.response.ApiResponse;
import com.community.global.response.SuccessMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController implements PostApiSpec {

    private final PostService postService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostSingleResponse>>> getPosts(
            @ModelAttribute PaginationRequest paginationRequest
            ) {
        PageResponse<PostSingleResponse> response = postService.getPostList(paginationRequest);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.POST_LIST_FETCHED, response));
    }

    @Override
    @Auth
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostIdResponse>> createPost(@AuthUser AuthenticatedUser authenticatedUser,
                                                                  @ModelAttribute @Valid PostCreateRequest request) {
        PostIdResponse res = postService.createPost(authenticatedUser.userId(), request);

        return ResponseEntity
                .created(UriUtil.makeLocationFromCurrent(res.getId()))
                .body(ApiResponse.success(SuccessMessage.POST_CREATED, res));
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostSingleResponse>> getPost(@PathVariable Long postId) {
        PostSingleResponse response = postService.viewPost(postId);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.POST_FETCHED, response));
    }

    @Override
    @Auth
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostIdResponse>> updatePost(@PathVariable Long postId,
                                                                  @AuthUser AuthenticatedUser authenticatedUser,
                                                                  @ModelAttribute @Valid PostUpdateRequest request) {
        PostIdResponse response = postService.updatePost(postId, authenticatedUser.userId(), request);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.POST_UPDATED, response));
    }

    @Override
    @Auth
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId,
                                                        @AuthUser AuthenticatedUser authenticatedUser) {
        postService.deletePost(postId, authenticatedUser.userId());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(SuccessMessage.POST_DELETED));
    }

    @Override
    @Auth
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> toggleLike(@PathVariable Long postId,
                                                        @AuthUser AuthenticatedUser authenticatedUser) {
        PostLikeResponse response = postService.toggleLike(postId, authenticatedUser.userId());
        String message = response.isLiked() ? SuccessMessage.POST_LIKED : SuccessMessage.POST_LIKE_CANCELLED;

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(message));
    }

    @Override
    @Auth
    @GetMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostLikeResponse>> isUserLikedPost(@PathVariable Long postId,
                                                                         @AuthUser AuthenticatedUser authenticatedUser) {
        PostLikeResponse res = postService.checkUserLikedPost(postId, authenticatedUser.userId());

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.POST_LIKE_STATUS_FETCHED, res));
    }
}
