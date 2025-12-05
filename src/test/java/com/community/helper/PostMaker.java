package com.community.helper;

import com.community.domain.board.model.Post;
import com.community.domain.user.model.User;

public abstract class PostMaker {

    public static Post getNumberedPost(User user, int num) {
        return new Post(user, "t" + num, "image" + num, "body" + num);
    }
}
