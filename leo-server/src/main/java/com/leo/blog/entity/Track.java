package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 音乐曲目（Music 页 / 全局播放器共用）。
 * cover_seed：封面渐变种子（0~N），未上传封面图时前端按种子生成占位封面。
 * url：真实音频访问路径；cover：真实封面图路径（均可空）。
 */
@Data
@TableName("t_track")
public class Track {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 曲名（中文） */
    private String title;

    /** 英文曲名 */
    private String enTitle;

    /** 艺术家 */
    private String artist;

    /** 时长（秒） */
    private Integer duration;

    /** 封面渐变种子（前端据此生成占位封面） */
    private Integer coverSeed;

    /** 音频访问路径（如 /uploads/music/xxx.mp3） */
    private String url;

    /** 封面图路径（为空时前端用 cover_seed 渐变） */
    private String cover;

    private LocalDateTime createdAt;
}
