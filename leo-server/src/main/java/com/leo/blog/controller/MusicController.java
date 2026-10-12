package com.leo.blog.controller;

import com.leo.blog.common.Result;
import com.leo.blog.common.TokenStore;
import com.leo.blog.dto.TrackVO;
import com.leo.blog.service.MusicService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 音乐接口：
 *  - GET  /api/music/tracks       —— 曲目列表（公开，播放器 / Music 页共用）
 *  - GET  /api/music/tracks/jsonp —— JSONP 列表（script 资源通道，绕过豆包浏览器 XHR/fetch 挂起 bug）
 *  - POST /api/music/tracks       —— 上传曲目（需登录；multipart：file + cover + title + enTitle + artist + duration）
 *  - DELETE /api/music/tracks/{id} —— 删除曲目（需登录）
 */
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;
    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper;

    /** GET /api/music/tracks */
    @GetMapping("/tracks")
    public Result<List<TrackVO>> tracks() {
        return Result.ok(musicService.listTracks());
    }

    /** GET /api/music/tracks/jsonp?callback=xxx —— JSONP 列表（script 标签加载，规避连接池挂起） */
    @GetMapping(value = "/tracks/jsonp", produces = "application/javascript;charset=UTF-8")
    public String tracksJsonp(@RequestParam("callback") String callback) throws Exception {
        Result<List<TrackVO>> result = Result.ok(musicService.listTracks());
        String json = objectMapper.writeValueAsString(result);
        return callback + "(" + json + ")";
    }

    /** POST /api/music/tracks —— 上传曲目（需登录） */
    @PostMapping("/tracks")
    public Result<TrackVO> upload(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String enTitle,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Integer duration) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        try {
            return Result.ok(musicService.uploadTrack(file, cover, title, enTitle, artist, duration));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(500, e.getMessage());
        }
    }

    /** DELETE /api/music/tracks/{id} —— 删除曲目（需登录） */
    @DeleteMapping("/tracks/{id}")
    public Result<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        musicService.deleteTrack(id);
        return Result.ok();
    }

    /** 登录态校验：无效时返回 401 Result，有效返回 null。 */
    private Result<Void> requireAdmin(String auth) {
        if (auth == null || !auth.startsWith("Bearer ") || !tokenStore.valid(auth.substring(7))) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        return null;
    }
}
