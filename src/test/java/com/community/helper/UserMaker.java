package com.community.helper;

import com.community.domain.user.model.User;
import org.springframework.test.util.ReflectionTestUtils;

public abstract class UserMaker {

    public static User getDefaultUser() {
        return getNumberedUser(1);
    }

    public static User getNumberedUser(int num) {
        return new User("test" + num + "@email.com", "password", "test" + num, "imageURL" + num);
    }

    public static User getIdentifyingUser(int num) {
        User user =  getNumberedUser(num);
        ReflectionTestUtils.setField(user, "id", num);

        return user;
    }
}
