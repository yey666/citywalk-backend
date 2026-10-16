package com.citywalk.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.dto.CommentVO;
import com.citywalk.backend.dto.PostRequest;
import com.citywalk.backend.dto.PostVO;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.entity.Post;
import com.citywalk.backend.entity.PostComment;
import com.citywalk.backend.entity.PostLike;
import com.citywalk.backend.entity.User;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.mapper.PostCommentMapper;
import com.citywalk.backend.mapper.PostLikeMapper;
import com.citywalk.backend.mapper.PostMapper;
import com.citywalk.backend.mapper.UserMapper;
import com.citywalk.backend.service.PostService;
import com.citywalk.backend.util.UserContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;
    private final UserMapper userMapper;
    private final PoiMapper poiMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> list(Long poiId, int page, int size) {
        LambdaQueryWrapper<Post> countWrapper = new LambdaQueryWrapper<Post>()
                .eq(poiId != null, Post::getPoiId, poiId);
        long total = postMapper.selectCount(countWrapper);

        int offset = (page - 1) * size;
        LambdaQueryWrapper<Post> listWrapper = new LambdaQueryWrapper<Post>()
                .eq(poiId != null, Post::getPoiId, poiId)
                .orderByDesc(Post::getId)
                .last("LIMIT " + size + " OFFSET " + offset);
        List<Post> posts = postMapper.selectList(listWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", toVos(posts));
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", (long) page * size < total);
        return result;
    }

    @Override
    public PostVO detail(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        return toVos(Collections.singletonList(post)).get(0);
    }

    @Override
    @Transactional
    public Long create(PostRequest request) {
        Long userId = UserContext.getUserId();
        Post post = new Post();
        post.setUserId(userId);
        post.setPoiId(request.getPoiId());
        post.setRouteId(request.getRouteId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(toJson(request.getImages()));
        post.setCreatedAt(LocalDateTime.now());
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    @Transactional
    public void like(Long postId) {
        Long userId = UserContext.getUserId();
        Long count = postLikeMapper.selectCount(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getUserId, userId)
                        .eq(PostLike::getPostId, postId)
        );
        if (count != null && count > 0) {
            return; // 已点过，幂等
        }
        PostLike like = new PostLike();
        like.setUserId(userId);
        like.setPostId(postId);
        like.setCreatedAt(LocalDateTime.now());
        postLikeMapper.insert(like);
    }

    @Override
    @Transactional
    public void unlike(Long postId) {
        Long userId = UserContext.getUserId();
        postLikeMapper.delete(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getUserId, userId)
                        .eq(PostLike::getPostId, postId)
        );
    }

    @Override
    public List<CommentVO> comments(Long postId) {
        List<PostComment> comments = postCommentMapper.selectList(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .orderByAsc(PostComment::getId)
        );
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> users = userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<CommentVO> vos = new ArrayList<>();
        for (PostComment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setPostId(c.getPostId());
            vo.setContent(c.getContent());
            vo.setCreatedAt(c.getCreatedAt());
            fillAuthor(vo, users.get(c.getUserId()));
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional
    public CommentVO addComment(Long postId, String content) {
        Long userId = UserContext.getUserId();
        PostComment comment = new PostComment();
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        postCommentMapper.insert(comment);

        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(userId);
        vo.setPostId(postId);
        vo.setContent(content);
        vo.setCreatedAt(comment.getCreatedAt());
        User user = userMapper.selectById(userId);
        fillAuthor(vo, user);
        return vo;
    }

    // ============ 内部方法 ============

    private List<PostVO> toVos(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, User> users = userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        Set<Long> poiIds = posts.stream().map(Post::getPoiId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Poi> pois = poiIds.isEmpty() ? Collections.emptyMap()
                : poiMapper.selectBatchIds(poiIds).stream().collect(Collectors.toMap(Poi::getId, Function.identity()));

        Long currentUserId = UserContext.getUserId();

        List<PostVO> vos = new ArrayList<>();
        for (Post p : posts) {
            PostVO vo = new PostVO();
            vo.setId(p.getId());
            vo.setUserId(p.getUserId());
            vo.setPoiId(p.getPoiId());
            vo.setRouteId(p.getRouteId());
            vo.setTitle(p.getTitle());
            vo.setContent(p.getContent());
            vo.setImages(fromJson(p.getImages()));
            vo.setCreatedAt(p.getCreatedAt());

            fillAuthor(vo, users.get(p.getUserId()));

            if (p.getPoiId() != null && pois.get(p.getPoiId()) != null) {
                vo.setPoiName(pois.get(p.getPoiId()).getName());
            }

            vo.setLikeCount(postLikeMapper.selectCount(
                    new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, p.getId())
            ).intValue());
            vo.setCommentCount(postCommentMapper.selectCount(
                    new LambdaQueryWrapper<PostComment>().eq(PostComment::getPostId, p.getId())
            ).intValue());

            vo.setLiked(false);
            if (currentUserId != null) {
                Long liked = postLikeMapper.selectCount(
                        new LambdaQueryWrapper<PostLike>()
                                .eq(PostLike::getUserId, currentUserId)
                                .eq(PostLike::getPostId, p.getId())
                );
                vo.setLiked(liked != null && liked > 0);
            }
            vos.add(vo);
        }
        return vos;
    }

    private void fillAuthor(PostVO vo, User user) {
        if (user == null) {
            return;
        }
        vo.setAuthorNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        vo.setAuthorAvatar(user.getAvatar());
    }

    private void fillAuthor(CommentVO vo, User user) {
        if (user == null) {
            return;
        }
        vo.setAuthorNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        vo.setAuthorAvatar(user.getAvatar());
    }

    private String toJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
