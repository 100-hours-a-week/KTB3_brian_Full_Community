package com.community.global.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SuccessMessage {

    // auth
    public static final String LOGIN_SUCCESS = "로그인에 성공하였습니다.";
    public static final String TOKEN_REFRESHED = "토큰이 갱신되었습니다.";

    // user
    public static final String SIGN_UP_SUCCESS = "회원가입에 성공했습니다.";
    public static final String SIGN_UP_INFO_AVAILABLE = "사용할 수 있는 정보입니다.";
    public static final String PROFILE_FETCHED = "사용자 정보 조회에 성공했습니다.";
    public static final String PROFILE_UPDATED = "사용자 정보가 수정되었습니다.";
    public static final String PASSWORD_UPDATED = "비밀번호가 수정되었습니다.";

    // post
    public static final String POST_LIST_FETCHED = "게시글 목록 조회에 성공했습니다.";
    public static final String POST_CREATED = "게시글이 등록되었습니다.";
    public static final String POST_FETCHED = "게시글 상세 조회에 성공했습니다.";
    public static final String POST_UPDATED = "게시글이 수정되었습니다.";
    public static final String POST_DELETED = "게시글이 삭제되었습니다.";
    public static final String POST_LIKED = "게시글에 좋아요를 표시했습니다.";
    public static final String POST_LIKE_CANCELLED = "게시글 좋아요가 취소되었습니다.";
    public static final String POST_LIKE_STATUS_FETCHED = "유저의 게시글 좋아요 여부 조회에 성공했습니다.";

    // comment
    public static final String COMMENT_LIST_FETCHED = "게시글 댓글 조회에 성공했습니다.";
    public static final String COMMENT_CREATED = "게시글 댓글 등록에 성공했습니다.";
    public static final String COMMENT_UPDATED = "게시글 댓글 수정에 성공했습니다.";
    public static final String COMMENT_DELETED = "게시글 댓글 삭제에 성공했습니다.";
}
