package com.example.canteen.service.impl;

import com.example.canteen.dto.auth.LoginRequest;
import com.example.canteen.dto.auth.RegisterRequest;
import com.example.canteen.dto.auth.UserInfoResponse;
import com.example.canteen.entity.AppUser;
import com.example.canteen.exception.BizException;
import com.example.canteen.repository.AppUserRepository;
import com.example.canteen.service.AuthService;
import com.example.canteen.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserInfoResponse register(RegisterRequest request) {
        if (appUserRepository.existsByStudentNo(request.getStudentNo())) {
            throw new BizException(4003, "学号已存在");
        }

        AppUser appUser = new AppUser();
        appUser.setStudentNo(request.getStudentNo().trim());
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUser.setNickname(request.getNickname().trim());
        appUser.setRole("USER");

        AppUser saved = appUserRepository.save(appUser);
        return toUserInfo(saved);
    }

    @Override
    public UserInfoResponse login(LoginRequest request, HttpSession session) {
        AppUser user = appUserRepository.findByStudentNo(request.getStudentNo().trim())
                .orElseThrow(() -> new BizException(4011, "学号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(4011, "学号或密码错误");
        }

        session.setAttribute(SessionUtil.LOGIN_USER_ID, user.getId());
        session.setMaxInactiveInterval(2 * 60 * 60);

        return toUserInfo(user);
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public UserInfoResponse getCurrentUser(HttpSession session) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) {
            throw new BizException(4010, "请先登录");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BizException(4012, "用户不存在或登录已失效"));
        return toUserInfo(user);
    }

    private UserInfoResponse toUserInfo(AppUser user) {
        return new UserInfoResponse(user.getId(), user.getStudentNo(), user.getNickname(), user.getRole());
    }
}
