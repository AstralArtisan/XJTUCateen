package com.example.canteen.service;

import com.example.canteen.dto.auth.LoginRequest;
import com.example.canteen.dto.auth.RegisterRequest;
import com.example.canteen.dto.auth.UserInfoResponse;
import jakarta.servlet.http.HttpSession;

public interface AuthService {

    UserInfoResponse register(RegisterRequest request);

    UserInfoResponse login(LoginRequest request, HttpSession session);

    void logout(HttpSession session);

    UserInfoResponse getCurrentUser(HttpSession session);
}
