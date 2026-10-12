package com.leo.blog.controller;

import com.leo.blog.common.Result;
import com.leo.blog.common.TokenStore;
import com.leo.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员登录接口：
 *  - POST /api/auth/login   校验账号密码，成功发放 token
 *  - POST /api/auth/logout  注销当前 token
 * 凭证存于 t_site_config（admin_user / admin_password）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SiteService siteService;
    private final TokenStore tokenStore;

    /** POST /api/auth/login  body: { "username": "...", "password": "..." } -> { "token": "..." } */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody(required = false) Map<String, String> body) {
        String username = body == null ? "" : body.getOrDefault("username", "");
        String password = body == null ? "" : body.getOrDefault("password", "");
        if (!siteService.verifyAdmin(username, password)) {
            return Result.error(400, "账号或密码错误");
        }
        return Result.ok(Map.of("token", tokenStore.create()));
    }

    /** POST /api/auth/logout  header: Authorization: Bearer <token> */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            tokenStore.revoke(auth.substring(7));
        }
        return Result.ok();
    }
}
