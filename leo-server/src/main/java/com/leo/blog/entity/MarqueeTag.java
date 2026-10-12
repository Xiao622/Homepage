package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 首页跑马灯标签（t_marquee_tag）。
 */
@Data
@TableName("t_marquee_tag")
public class MarqueeTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer sort;
}
