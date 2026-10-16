package com.citywalk.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_comment")
public class PostComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
}
