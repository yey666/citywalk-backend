package com.citywalk.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citywalk.backend.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
}
