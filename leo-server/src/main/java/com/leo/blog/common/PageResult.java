package com.leo.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果：{ list, total, page, pageSize }。
 * 与前端 blogApi.getPosts() 的返回结构一致。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long page;
    private long pageSize;
}
