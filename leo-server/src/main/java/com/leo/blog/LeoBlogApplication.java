package com.leo.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Leo 个人站点后端入口。
 */
@SpringBootApplication
@MapperScan("com.leo.blog.mapper")
public class LeoBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeoBlogApplication.class, args);
    }
}
