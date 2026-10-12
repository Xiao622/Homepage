package com.leo.blog.dto;

import lombok.Data;

/**
 * 相册照片展示对象。
 */
@Data
public class PhotoVO {

    private Long id;

    private String url;

    private String category;

    private String takenDate;

    private String caption;
}
