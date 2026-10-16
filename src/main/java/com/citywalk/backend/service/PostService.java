package com.citywalk.backend.service;

import com.citywalk.backend.dto.CommentVO;
import com.citywalk.backend.dto.PostRequest;
import com.citywalk.backend.dto.PostVO;

import java.util.List;
import java.util.Map;

public interface PostService {

    Map<String, Object> list(Long poiId, int page, int size);

    PostVO detail(Long id);

    Long create(PostRequest request);

    void like(Long postId);

    void unlike(Long postId);

    List<CommentVO> comments(Long postId);

    CommentVO addComment(Long postId, String content);
}
