package com.example.canteen.util;

import com.example.canteen.exception.BizException;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {

    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";

    private SessionUtil() {
    }

    public static Long getLoginUserId(HttpSession session) {
        Object val = session.getAttribute(LOGIN_USER_ID);
        if (val instanceof Long id) {
            return id;
        }
        if (val instanceof Integer id) {
            return id.longValue();
        }
        return null;
    }

    public static Long requireLogin(HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            throw new BizException(4010, "请先登录");
        }
        return userId;
    }
}
