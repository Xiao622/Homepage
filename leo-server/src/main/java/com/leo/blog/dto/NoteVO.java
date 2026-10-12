package com.leo.blog.dto;

import lombok.Data;

/**
 * 随记展示对象（字段与前端 notes 数据一致）。
 */
@Data
public class NoteVO {

    private Long id;

    private String date;

    private String title;

    private String text;

    private Boolean expandable;
}
