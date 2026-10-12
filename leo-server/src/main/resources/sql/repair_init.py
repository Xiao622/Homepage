# -*- coding: utf-8 -*-
"""修复 init.sql：整理 13 篇种子（移除错位 fragment、补回 t_post/t_tag 建表、替换旧种子块）。"""
import pymysql

base = r"C:\Users\Dell\DoubaoWork\chats\2026-09-22\new-chat-1\leo-server\src\main\resources\sql"

# 1) 从数据库取 t_post / t_tag 精确 DDL
conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='888888', database='leo_blog', charset='utf8mb4')
cur = conn.cursor()
cur.execute("SHOW CREATE TABLE t_post")
ddl_post = cur.fetchone()[1]
cur.execute("SHOW CREATE TABLE t_tag")
ddl_tag = cur.fetchone()[1]
conn.close()

# 2) 读 fragment 与 init.sql
with open(base + r"\seed_posts_fragment.sql", "r", encoding="utf-8") as f:
    frag = f.read().strip()
with open(base + r"\init.sql", "r", encoding="utf-8") as f:
    sql = f.read()

# 3) 移除错位块（行 27-46：fragment 被插在建表区中间）
mis_start = "-- ---------- 博客文章：13 篇开发记录"
i0 = sql.index(mis_start)
# 错位块结束：标签云 INSERT 分号后的换行
tag_insert = "INSERT INTO `t_tag` (`name`, `count`) VALUES\n('About', 1),"
i1 = sql.index(";\n\n", sql.index(tag_insert, i0)) + 3
sql = sql[:i0] + sql[i1:]

# 4) 在 t_marquee_tag 建表之后补回 t_post / t_tag 建表
anchor = ") ENGINE = InnoDB COMMENT = '首页技术栈跑马灯标签';\n\n"
j = sql.index(anchor) + len(anchor)
ddls = (
    "-- ---------- 博客文章 ----------\n"
    + ddl_post + "\n\n"
    + "-- ---------- 标签云 ----------\n"
    + ddl_tag + "\n\n"
)
sql = sql[:j] + ddls + sql[j:]

# 5) 替换旧种子块（1 篇大记录 + 旧标签云）为 fragment
old_start = "-- 博客文章（slug 为路由编号：新建文章 = 年月日时分）"
old_end_marker = "('本站', 1), ('开发', 1), ('Vue', 1), ('Spring Boot', 1), ('Leo', 1);\n"
k0 = sql.index(old_start)
k1 = sql.index(old_end_marker, k0) + len(old_end_marker)
# 顺带吞掉后面空行
if sql[k1:k1 + 2] == "\n\n":
    k1 += 1
sql = sql[:k0] + frag + "\n\n" + sql[k1:]

# 6) 写回
with open(base + r"\init.sql", "w", encoding="utf-8") as f:
    f.write(sql)
print("repaired, len:", len(sql))
