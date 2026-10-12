package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 相册照片（t_photo）。
 */
@Data
@TableName("t_photo")
public class Photo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访问路径（如 /uploads/photos/xxx.jpg） */
    private String url;

    /** 分类名（自由文本，如 daily / 街景） */
    private String category;

    /** 拍摄时间（如 2026-09、2026-09-12） */
    @TableField("taken_date")
    private String takenDate;

    /** 说明文字 */
    private String caption;

    @TableField("created_at")
    private java.time.LocalDateTime createdAt;
}
