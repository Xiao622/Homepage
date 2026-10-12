package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 博客文章（t_post）。
 */
@Data
@TableName("t_post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 路由编号（年月日时分，如 202609292009；详情页 URL 使用） */
    private String slug;

    /** 发布日期，展示用（如 2026.09.16） */
    @TableField("post_date")
    private String date;

    private String title;

    private String excerpt;

    /** 正文（详情页展示；可为空则前端回退摘要） */
    private String content;

    /** 阅读时长（分钟） */
    private Integer minutes;

    /** 标签，逗号分隔（FIND_IN_SET 查询） */
    private String tags;

    /** 创建时间（由数据库维护） */
    @TableField("created_at")
    private java.time.LocalDateTime createdAt;
}
