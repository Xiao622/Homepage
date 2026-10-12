package com.leo.blog.dto;

import lombok.Data;

/**
 * 曲目对外数据结构（Music 页 / 播放器）。
 */
@Data
public class TrackVO {

    private Long id;

    /** 曲名（中文） */
    private String title;

    /** 英文曲名 */
    private String enTitle;

    /** 艺术家 */
    private String artist;

    /** 时长（秒） */
    private Integer duration;

    /** 封面渐变种子 */
    private Integer coverSeed;

    /** 音频访问路径（可空：占位曲目无真实音频） */
    private String url;

    /** 封面图路径（可空：前端用 coverSeed 渐变） */
    private String cover;
}
