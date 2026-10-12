package com.leo.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章展示对象：tags 由库中 CSV 转为数组，与前端结构一致。
 * 列表接口不返回 content（保持轻量）；详情接口单独填充。
 */
@Data
public class PostVO {

    private Long id;

    /** 路由编号（年月日时分），详情页 URL 使用 */
    private String slug;

    private String date;

    private String title;

    private String excerpt;

    /** 正文（仅详情接口返回） */
    private String content;

    private Integer minutes;

    private List<String> tags;
}
