package com.leo.blog.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易登录令牌存储（演示用）：
 * 登录成功发放 UUID token，内存保存并设 30 分钟过期。
 * 生产环境建议替换为 JWT / Redis 会话方案。
 */
@Component
public class TokenStore {

    private static final long TTL_MILLIS = 30 * 60 * 1000L;

    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    /** 发放新 token，返回 token 字符串。 */
    public String create() {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, System.currentTimeMillis() + TTL_MILLIS);
        return token;
    }

    /** 校验 token 是否有效（存在且未过期）；过期自动清除。 */
    public boolean valid(String token) {
        if (token == null || token.isBlank()) return false;
        Long expire = tokens.get(token);
        if (expire == null) return false;
        if (System.currentTimeMillis() > expire) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    /** 注销 token。 */
    public void revoke(String token) {
        if (token != null) tokens.remove(token);
    }
}
