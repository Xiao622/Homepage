package com.leo.blog.dto;

import lombok.Data;

/**
 * 站点信息展示对象（与前端 siteInfo 字段一一对应）。
 */
@Data
public class SiteInfoVO {

    private String name;
    private String motto;
    private String mottoOrigin;
    private String footer;
    private String rss;
    private String github;
    private String email;
}
