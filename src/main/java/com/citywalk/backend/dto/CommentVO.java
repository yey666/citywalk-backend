package com.citywalk.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
    private String authorNickname;
    private String authorAvatar;
}
