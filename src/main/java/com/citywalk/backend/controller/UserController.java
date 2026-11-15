package com.citywalk.backend.controller;

import com.citywalk.backend.service.UserService;
import com.citywalk.backend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户", description = "个人资料")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "更新昵称")
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        userService.updateProfile(userId, body.get("nickname"));
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
