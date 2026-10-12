package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 照片评论（t_photo_comment）。
 */
@Data
@TableName("t_photo_comment")
public class PhotoComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属照片 id */
    private Long photoId;

    /** 评论者昵称 */
    private String author;

    /** 评论内容 */
    private String content;

    private java.time.LocalDateTime createdAt;
}
