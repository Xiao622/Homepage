package com.leo.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 标签云（t_tag），name 为主键，count 为该标签下文章数。
 */
@Data
@TableName("t_tag")
public class Tag {

    @TableId
    private String name;

    private Integer count;
}
