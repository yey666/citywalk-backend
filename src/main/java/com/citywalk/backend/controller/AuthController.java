package com.citywalk.backend.controller;

import com.citywalk.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        return userService.register(
                body.get("username"),
                body.get("password"),
                body.get("nickname")
        );
    }

    /**
     * 登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return userService.login(
                body.get("username"),
                body.get("password")
        );
    }
}