package com.example.canteen.controller;

import com.example.canteen.common.ApiResponse;
import com.example.canteen.dto.auth.LoginRequest;
import com.example.canteen.dto.auth.RegisterRequest;
import com.example.canteen.dto.auth.UserInfoResponse;
import com.example.canteen.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserInfoResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<UserInfoResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.success("登录成功", authService.login(request, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.success("退出登录成功", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(HttpSession session) {
        return ApiResponse.success(authService.getCurrentUser(session));
    }
}
