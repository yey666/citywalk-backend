package com.citywalk.backend.controller;

import com.citywalk.backend.dto.CommentVO;
import com.citywalk.backend.dto.PostRequest;
import com.citywalk.backend.dto.PostVO;
import com.citywalk.backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "社区", description = "帖子、点赞、评论")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "帖子列表（可按 POI 筛选）")
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long poiId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return postService.list(poiId, page, size);
    }

    @Operation(summary = "帖子详情")
    @GetMapping("/{id}")
    public PostVO detail(@PathVariable Long id) {
        return postService.detail(id);
    }

    @Operation(summary = "发帖")
    @PostMapping
    public Map<String, Object> create(@RequestBody PostRequest request) {
        Long id = postService.create(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", id);
        return result;
    }

    @Operation(summary = "点赞")
    @PostMapping("/{id}/like")
    public Map<String, Object> like(@PathVariable Long id) {
        postService.like(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Map<String, Object> unlike(@PathVariable Long id) {
        postService.unlike(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @Operation(summary = "发评论")
    @PostMapping("/{id}/comment")
    public CommentVO comment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return postService.addComment(id, body.get("content"));
    }

    @Operation(summary = "评论列表")
    @GetMapping("/{id}/comments")
    public List<CommentVO> comments(@PathVariable Long id) {
        return postService.comments(id);
    }
}
