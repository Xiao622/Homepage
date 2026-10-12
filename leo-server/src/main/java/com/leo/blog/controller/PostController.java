package com.leo.blog.controller;

import com.leo.blog.common.PageResult;
import com.leo.blog.common.Result;
import com.leo.blog.common.TokenStore;
import com.leo.blog.dto.PostVO;
import com.leo.blog.entity.Tag;
import com.leo.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章接口：
 *  - 查询（公开）：分页列表 / 标签云 / 按 slug 详情
 *  - 写操作（需登录 token）：新建（自动生成 slug）、按 slug 更新 / 删除
 * 未登录或 token 过期返回 code=401。
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final TokenStore tokenStore;

    /** GET /api/posts?page=1&pageSize=6&tag=本站&keyword=xxx */
    @GetMapping
    public Result<PageResult<PostVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "6") long pageSize,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        return Result.ok(postService.pagePosts(page, pageSize, tag, keyword));
    }

    /** GET /api/posts/tags */
    @GetMapping("/tags")
    public Result<List<Tag>> tags() {
        return Result.ok(postService.listTags());
    }

    /** GET /api/posts/{slug} —— 文章详情（按路由编号，公开） */
    @GetMapping("/{slug}")
    public Result<PostVO> detail(@PathVariable String slug) {
        PostVO post = postService.getPostBySlug(slug);
        if (post == null) {
            return Result.error(404, "文章不存在");
        }
        return Result.ok(post);
    }

    /** POST /api/posts —— 新建文章（需登录；slug 由后端自动生成） */
    @PostMapping
    public Result<PostVO> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody(required = false) PostVO body) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        if (body == null || !StringUtils.hasText(body.getTitle()) || !StringUtils.hasText(body.getDate())) {
            return Result.error(400, "标题和日期不能为空");
        }
        return Result.ok(postService.createPost(body));
    }

    /** PUT /api/posts/{slug} —— 更新文章（需登录） */
    @PutMapping("/{slug}")
    public Result<PostVO> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String slug,
            @RequestBody(required = false) PostVO body) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        if (body == null || !StringUtils.hasText(body.getTitle()) || !StringUtils.hasText(body.getDate())) {
            return Result.error(400, "标题和日期不能为空");
        }
        try {
            return Result.ok(postService.updatePost(slug, body));
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /** DELETE /api/posts/{slug} —— 删除文章（需登录；正文图片一并清理） */
    @DeleteMapping("/{slug}")
    public Result<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String slug) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        postService.deletePost(slug);
        return Result.ok();
    }

    /** POST /api/posts/images —— 上传正文图片（需登录；multipart file，返回访问路径） */
    @PostMapping("/images")
    public Result<String> uploadImage(
            @RequestHeader(value = "Authorization", required = false) String auth,
            org.springframework.web.multipart.MultipartFile file) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        try {
            return Result.ok(postService.saveImage(file));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 登录态校验：无效时返回 401 Result，有效返回 null。 */
    private Result<Void> requireAdmin(String auth) {
        if (auth == null || !auth.startsWith("Bearer ") || !tokenStore.valid(auth.substring(7))) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        return null;
    }
}
