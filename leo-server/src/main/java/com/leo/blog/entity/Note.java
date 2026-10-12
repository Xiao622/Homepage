package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 随记（t_note）。
 */
@Data
@TableName("t_note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日期，展示用（如 09-11 / 2026-9-17） */
    @TableField("note_date")
    private String date;

    /** 可选标题 */
    private String title;

    private String text;

    /** 是否可展开 */
    private Boolean expandable;

    @TableField("created_at")
    private java.time.LocalDateTime createdAt;
}
