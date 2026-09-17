package com.citywalk.backend.service;

import com.citywalk.backend.entity.User;

import java.util.Map;

public interface UserService {

    /**
     * 注册，返回 token
     */
    Map<String, Object> register(String username, String password, String nickname);

    /**
     * 登录，返回 token
     */
    Map<String, Object> login(String username, String password);

    /**
     * 根据 id 查用户
     */
    User getById(Long id);
}