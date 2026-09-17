package com.citywalk.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.entity.User;
import com.citywalk.backend.mapper.UserMapper;
import com.citywalk.backend.service.UserService;
import com.citywalk.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> register(String username, String password, String nickname) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setRole("user");
        userMapper.insert(user);

        // 3. 生成 token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("token", token);
        return result;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 查用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成 token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("token", token);
        return result;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}