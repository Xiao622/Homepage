package com.leo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leo.blog.dto.SiteInfoVO;
import com.leo.blog.entity.MarqueeTag;
import com.leo.blog.entity.SiteConfig;
import com.leo.blog.mapper.MarqueeTagMapper;
import com.leo.blog.mapper.SiteConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站点信息查询服务：基础配置、关于页、跑马灯标签。
 */
@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteConfigMapper siteConfigMapper;
    private final MarqueeTagMapper marqueeTagMapper;
    private final ObjectMapper objectMapper;

    /**
     * 站点基础信息（name / motto / footer / rss / github / email ...）。
     */
    public SiteInfoVO getSiteInfo() {
        Map<String, String> kv = siteConfigMapper.selectList(null).stream()
                .collect(Collectors.toMap(SiteConfig::getConfigKey,
                        s -> s.getConfigValue() == null ? "" : s.getConfigValue()));

        SiteInfoVO vo = new SiteInfoVO();
        vo.setName(get(kv, "name"));
        vo.setMotto(get(kv, "motto"));
        vo.setMottoOrigin(get(kv, "motto_origin"));
        vo.setFooter(get(kv, "footer"));
        vo.setRss(get(kv, "rss"));
        vo.setGithub(get(kv, "github"));
        vo.setEmail(get(kv, "email"));
        return vo;
    }

    /**
     * 关于页完整数据（t_site_config.about 中存储整体 JSON，原样解析返回）。
     */
    public Object getAbout() {
        SiteConfig cfg = siteConfigMapper.selectById("about");
        if (cfg == null || !StringUtils.hasText(cfg.getConfigValue())) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(cfg.getConfigValue(), Object.class);
        } catch (Exception e) {
            // JSON 损坏时降级为空对象，避免接口 500
            return Map.of();
        }
    }

    /**
     * 保存关于页数据（整体覆盖 t_site_config.about）。
     */
    public void updateAbout(Map<String, Object> content) {
        if (content == null) {
            return;
        }
        SiteConfig cfg = new SiteConfig();
        cfg.setConfigKey("about");
        try {
            cfg.setConfigValue(objectMapper.writeValueAsString(content));
        } catch (Exception e) {
            throw new RuntimeException("序列化 about 失败", e);
        }
        upsert(cfg);
    }

    /**
     * 首页标签跑马灯（按 sort 升序）。
     */
    public List<String> listMarqueeTags() {
        return marqueeTagMapper.selectList(
                        new LambdaQueryWrapper<MarqueeTag>().orderByAsc(MarqueeTag::getSort))
                .stream()
                .map(MarqueeTag::getName)
                .collect(Collectors.toList());
    }

    /**
     * 管理员凭证校验：
     *  1) 优先读取 t_site_config.admin_accounts（多账户 JSON 数组）
     *     [{ "username": "...", "password": "..." }, ...]
     *  2) 该键缺失或损坏时回退单账户校验（admin_user / admin_password）
     * 演示项目明文存储；生产环境应改为哈希 + 盐。
     */
    public boolean verifyAdmin(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return false;
        }
        SiteConfig accountsCfg = siteConfigMapper.selectById("admin_accounts");
        if (accountsCfg != null && StringUtils.hasText(accountsCfg.getConfigValue())) {
            try {
                List<Map<String, String>> accounts = objectMapper.readValue(
                        accountsCfg.getConfigValue(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {
                        });
                for (Map<String, String> acc : accounts) {
                    if (username.equals(acc.get("username")) && password.equals(acc.get("password"))) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                // admin_accounts JSON 损坏时回退到单账户校验
            }
        }
        SiteConfig userCfg = siteConfigMapper.selectById("admin_user");
        SiteConfig passCfg = siteConfigMapper.selectById("admin_password");
        if (userCfg == null || passCfg == null) {
            return false;
        }
        return username.equals(userCfg.getConfigValue())
                && password.equals(passCfg.getConfigValue());
    }

    /**
     * Home 页可编辑文字（t_site_config.home_content，整体 JSON 返回）。
     */
    public Map<String, Object> getHomeContent() {
        SiteConfig cfg = siteConfigMapper.selectById("home_content");
        if (cfg == null || !StringUtils.hasText(cfg.getConfigValue())) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(cfg.getConfigValue(), Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * 保存 Home 页可编辑文字（整体覆盖 home_content）。
     * 若内容中含 footer，同步写入 t_site_config.footer（页脚卡片读取该键）。
     */
    public void updateHomeContent(Map<String, Object> content) {
        if (content == null || content.isEmpty()) {
            return;
        }
        SiteConfig cfg = new SiteConfig();
        cfg.setConfigKey("home_content");
        try {
            cfg.setConfigValue(objectMapper.writeValueAsString(content));
        } catch (Exception e) {
            throw new RuntimeException("序列化 home_content 失败", e);
        }
        upsert(cfg);

        Object footer = content.get("footer");
        if (footer != null) {
            SiteConfig f = new SiteConfig();
            f.setConfigKey("footer");
            f.setConfigValue(String.valueOf(footer));
            upsert(f);
        }
    }

    /** 存在则更新，不存在则插入（以 config_key 为主键）。 */
    private void upsert(SiteConfig cfg) {
        SiteConfig old = siteConfigMapper.selectById(cfg.getConfigKey());
        if (old == null) {
            siteConfigMapper.insert(cfg);
        } else {
            siteConfigMapper.updateById(cfg);
        }
    }

    private String get(Map<String, String> kv, String key) {
        return kv.getOrDefault(key, "");
    }
}
