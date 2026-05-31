package com.xjtu.canteen.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenUtil tokenUtil;

    public AuthInterceptor(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return true;
        }
        Map<String, Object> payload = tokenUtil.parseToken(auth.substring(7));
        if (payload != null) {
            request.setAttribute("auth_user_id", ((Number) payload.get("user_id")).longValue());
            request.setAttribute("auth_role", ((Number) payload.get("role")).intValue());
        }
        return true;
    }
}
