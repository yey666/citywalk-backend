package com.citywalk.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citywalk.backend.dto.CommentVO;
import com.citywalk.backend.dto.PostRequest;
import com.citywalk.backend.dto.PostVO;
import com.citywalk.backend.entity.Poi;
import com.citywalk.backend.entity.Post;
import com.citywalk.backend.entity.PostCollect;
import com.citywalk.backend.entity.PostComment;
import com.citywalk.backend.entity.PostLike;
import com.citywalk.backend.entity.Route;
import com.citywalk.backend.entity.User;
import com.citywalk.backend.mapper.PoiMapper;
import com.citywalk.backend.mapper.PostCollectMapper;
import com.citywalk.backend.mapper.PostCommentMapper;
import com.citywalk.backend.mapper.PostLikeMapper;
import com.citywalk.backend.mapper.PostMapper;
import com.citywalk.backend.mapper.RouteMapper;
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
    private final PostCollectMapper postCollectMapper;
    private final UserMapper userMapper;
    private final PoiMapper poiMapper;
    private final RouteMapper routeMapper;

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
    @Transactional
    public void collect(Long postId) {
        Long userId = UserContext.getUserId();
        Long count = postCollectMapper.selectCount(
                new LambdaQueryWrapper<PostCollect>()
                        .eq(PostCollect::getUserId, userId)
                        .eq(PostCollect::getPostId, postId)
        );
        if (count != null && count > 0) {
            return; // 已收藏，幂等
        }
        PostCollect collect = new PostCollect();
        collect.setUserId(userId);
        collect.setPostId(postId);
        collect.setCreatedAt(LocalDateTime.now());
        postCollectMapper.insert(collect);
    }

    @Override
    @Transactional
    public void uncollect(Long postId) {
        Long userId = UserContext.getUserId();
        postCollectMapper.delete(
                new LambdaQueryWrapper<PostCollect>()
                        .eq(PostCollect::getUserId, userId)
                        .eq(PostCollect::getPostId, postId)
        );
    }

    @Override
    public List<PostVO> myPosts() {
        Long userId = UserContext.getUserId();
        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
                        .orderByDesc(Post::getId)
        );
        return toVos(posts);
    }

    @Override
    public List<PostVO> myCollects() {
        Long userId = UserContext.getUserId();
        List<PostCollect> collects = postCollectMapper.selectList(
                new LambdaQueryWrapper<PostCollect>()
                        .eq(PostCollect::getUserId, userId)
                        .orderByDesc(PostCollect::getId)
        );
        if (collects.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = collects.stream().map(PostCollect::getPostId).collect(Collectors.toList());
        List<Post> posts = postMapper.selectBatchIds(postIds);
        // 保持收藏时间倒序
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, Function.identity()));
        List<Post> ordered = new ArrayList<>();
        for (PostCollect c : collects) {
            Post p = postMap.get(c.getPostId());
            if (p != null) ordered.add(p);
        }
        return toVos(ordered);
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

        Set<Long> routeIds = posts.stream().map(Post::getRouteId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Route> routes = routeIds.isEmpty() ? Collections.emptyMap()
                : routeMapper.selectBatchIds(routeIds).stream().collect(Collectors.toMap(Route::getId, Function.identity()));

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

            // 标签与天数：优先从关联行程推导
            if (p.getRouteId() != null && routes.get(p.getRouteId()) != null) {
                vo.setTag("跨城大环线");
                vo.setDays(computeDays(routes.get(p.getRouteId()).getTripDataJson()));
            } else if (p.getPoiId() != null) {
                vo.setTag("城市漫游");
            } else {
                vo.setTag("游记");
            }

            vo.setLikeCount(postLikeMapper.selectCount(
                    new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, p.getId())
            ).intValue());
            vo.setCommentCount(postCommentMapper.selectCount(
                    new LambdaQueryWrapper<PostComment>().eq(PostComment::getPostId, p.getId())
            ).intValue());
            vo.setCollectCount(postCollectMapper.selectCount(
                    new LambdaQueryWrapper<PostCollect>().eq(PostCollect::getPostId, p.getId())
            ).intValue());

            vo.setLiked(false);
            vo.setCollected(false);
            if (currentUserId != null) {
                Long liked = postLikeMapper.selectCount(
                        new LambdaQueryWrapper<PostLike>()
                                .eq(PostLike::getUserId, currentUserId)
                                .eq(PostLike::getPostId, p.getId())
                );
                vo.setLiked(liked != null && liked > 0);

                Long collected = postCollectMapper.selectCount(
                        new LambdaQueryWrapper<PostCollect>()
                                .eq(PostCollect::getUserId, currentUserId)
                                .eq(PostCollect::getPostId, p.getId())
                );
                vo.setCollected(collected != null && collected > 0);
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

    // 从 tripDataJson 里数城市节点，推导“3天2晚”之类的天数文案
    private String computeDays(String tripDataJson) {
        if (tripDataJson == null || tripDataJson.isBlank()) {
            return null;
        }
        try {
            List<Map<String, Object>> nodes = objectMapper.readValue(
                    tripDataJson, new TypeReference<List<Map<String, Object>>>() {});
            long cityCount = nodes.stream().filter(n -> "city".equals(n.get("type"))).count();
            if (cityCount >= 2) {
                return cityCount + "天" + (cityCount - 1) + "晚";
            }
            if (cityCount == 1) {
                return "1天";
            }
        } catch (Exception e) {
            // 解析失败就不展示天数
        }
        return null;
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
