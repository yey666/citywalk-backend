package com.citywalk.backend.config;

import com.citywalk.backend.util.JwtUtil;
import com.citywalk.backend.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        boolean isGet = "GET".equalsIgnoreCase(request.getMethod());

        // 没有 Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isGet) return true;
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未登录\"}");
            return false;
        }

        // 有 Authorization —— 解析 token
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        if (userId == null) {
            if (isGet) return true;
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"token 无效或已过期\"}");
            return false;
        }

        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}