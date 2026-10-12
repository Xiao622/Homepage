package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 站点配置项（t_site_config），key-value 结构。
 */
@Data
@TableName("t_site_config")
public class SiteConfig {

    @TableId("config_key")
    private String configKey;

    private String configValue;
}
