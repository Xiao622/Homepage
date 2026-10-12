package com.leo.blog.controller;

import com.leo.blog.common.Result;
import com.leo.blog.common.TokenStore;
import com.leo.blog.dto.SiteInfoVO;
import com.leo.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 站点信息接口：基础配置、关于页、跑马灯标签、Home 页可编辑文字。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final TokenStore tokenStore;

    /** GET /api/site */
    @GetMapping("/site")
    public Result<SiteInfoVO> info() {
        return Result.ok(siteService.getSiteInfo());
    }

    /** GET /api/site/marquee-tags */
    @GetMapping("/site/marquee-tags")
    public Result<List<String>> marqueeTags() {
        return Result.ok(siteService.listMarqueeTags());
    }

    /** GET /api/about */
    @GetMapping("/about")
    public Result<Object> about() {
        return Result.ok(siteService.getAbout());
    }

    /** PUT /api/about —— 保存关于页数据（需登录 token，整体覆盖） */
    @PutMapping("/about")
    public Result<Void> updateAbout(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody(required = false) Map<String, Object> content) {
        if (auth == null || !auth.startsWith("Bearer ") || !tokenStore.valid(auth.substring(7))) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        siteService.updateAbout(content);
        return Result.ok();
    }

    /** GET /api/site/home-content —— Home 页可编辑文字（无需登录） */
    @GetMapping("/site/home-content")
    public Result<Map<String, Object>> homeContent() {
        return Result.ok(siteService.getHomeContent());
    }

    /** PUT /api/site/home-content —— 保存 Home 页可编辑文字（需登录 token） */
    @PutMapping("/site/home-content")
    public Result<Void> updateHomeContent(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody(required = false) Map<String, Object> content) {
        if (auth == null || !auth.startsWith("Bearer ") || !tokenStore.valid(auth.substring(7))) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        siteService.updateHomeContent(content);
        return Result.ok();
    }
}
