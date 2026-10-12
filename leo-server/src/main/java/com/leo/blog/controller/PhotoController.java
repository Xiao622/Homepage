package com.leo.blog.controller;

import com.leo.blog.common.Result;
import com.leo.blog.common.TokenStore;
import com.leo.blog.dto.PhotoVO;
import com.leo.blog.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 相册照片接口：
 *  - 查询（公开）：相册列表、照片列表（分类 / 年月筛选）、分类统计、照片评论
 *  - 写操作（需登录 token）：上传（multipart）、按 id 更新信息、删除
 *  - 评论（公开）：访客可对照片发表评论
 */
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final TokenStore tokenStore;

    /** GET /api/photos?category=街&month=2026-09&limit=4 */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer limit) {
        return Result.ok(photoService.listPhotos(category, month, limit));
    }

    /** GET /api/photos/categories —— 分类统计（按照片数倒序） */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.ok(photoService.listCategories());
    }

    /** GET /api/photos/albums —— 相册列表（名称 / 照片数 / 封面，公开） */
    @GetMapping("/albums")
    public Result<List<Map<String, Object>>> albums() {
        return Result.ok(photoService.listAlbums());
    }

    /** GET /api/photos/{id}/comments —— 照片评论列表（公开，时间正序） */
    @GetMapping("/{id}/comments")
    public Result<List<Map<String, Object>>> comments(@PathVariable Long id) {
        return Result.ok(photoService.listComments(id));
    }

    /** POST /api/photos/{id}/comments —— 发表照片评论（公开；body：{author, content}） */
    @PostMapping("/{id}/comments")
    public Result<Map<String, Object>> comment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String author = body == null ? null : body.get("author");
            String content = body == null ? null : body.get("content");
            return Result.ok(photoService.addComment(id, author, content));
        } catch (IllegalArgumentException e) {
            if ("照片不存在".equals(e.getMessage())) {
                return Result.error(404, e.getMessage());
            }
            return Result.error(400, e.getMessage());
        }
    }

    /** POST /api/photos —— 上传照片（需登录；multipart：file + category + takenDate + caption） */
    @PostMapping
    public Result<PhotoVO> upload(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String takenDate,
            @RequestParam(required = false) String caption) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        try {
            return Result.ok(photoService.uploadPhoto(file, category, takenDate, caption));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(500, e.getMessage());
        }
    }

    /** PUT /api/photos/{id} —— 更新照片信息（分类 / 拍摄时间 / 说明，需登录） */
    @PutMapping("/{id}")
    public Result<PhotoVO> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @RequestBody(required = false) PhotoVO body) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        try {
            return Result.ok(photoService.updatePhoto(id, body));
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /** DELETE /api/photos/{id} —— 删除照片（需登录） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        photoService.deletePhoto(id);
        return Result.ok();
    }

    /** GET /uploads/{filename} —— 照片文件流（公开；upload 目录文件回放） */
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads").resolve(filename).toAbsolutePath().normalize();
            if (!file.startsWith(Paths.get("uploads").toAbsolutePath().normalize()) || !Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            MediaType type = MediaType.APPLICATION_OCTET_STREAM;
            String probed = Files.probeContentType(file);
            if (probed != null) {
                try {
                    type = MediaType.parseMediaType(probed);
                } catch (Exception ignore) {
                }
            }
            return ResponseEntity.ok()
                    .contentType(type)
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
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
