package com.example.canteen.config;

import com.example.canteen.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && SessionUtil.getLoginUserId(session) != null;
        if (loggedIn) {
            return true;
        }

        String uri = request.getRequestURI();
        if (uri.matches("^/api/windows/\\d+/reviews$") && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (uri.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":4010,\"message\":\"请先登录\",\"data\":null,\"timestamp\":\""
                            + LocalDateTime.now() + "\"}"
            );
            return false;
        }

        response.sendRedirect("/login");
        return false;
    }
}
