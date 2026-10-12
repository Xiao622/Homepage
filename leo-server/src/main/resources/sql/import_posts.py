# -*- coding: utf-8 -*-
"""导入 13 篇开发记录到 leo_blog，并生成 init.sql 的 t_post/t_tag 种子片段。"""
import sys, io, pymysql
sys.path.insert(0, r"C:\Users\Dell\DoubaoWork\chats\2026-09-22\new-chat-1\leo-server\src\main\resources\sql")
from posts_data import POSTS

conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='888888', database='leo_blog', charset='utf8mb4')
cur = conn.cursor()

# 1) 清空旧文章与标签
cur.execute("DELETE FROM t_post")
cur.execute("ALTER TABLE t_post AUTO_INCREMENT = 1")
cur.execute("DELETE FROM t_tag")

# 2) 插入文章（id 按列表顺序）
for i, p in enumerate(POSTS, start=1):
    cur.execute(
        "INSERT INTO t_post (id, slug, post_date, title, excerpt, minutes, tags, content) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)",
        (i, p["slug"], p["date"], p["title"], p["excerpt"], p["minutes"], p["tags"], p["content"])
    )

# 3) 重建标签云：合并所有文章 tags 并计数
tag_count = {}
for p in POSTS:
    for t in p["tags"].split(","):
        t = t.strip()
        if t:
            tag_count[t] = tag_count.get(t, 0) + 1
cur.executemany("INSERT INTO t_tag (name, count) VALUES (%s, %s)", sorted(tag_count.items()))

conn.commit()
cur.execute("SELECT COUNT(*), MIN(post_date), MAX(post_date) FROM t_post")
print("posts:", cur.fetchone())
cur.execute("SELECT name, count FROM t_tag ORDER BY count DESC, name")
print("tags:", cur.fetchall())
conn.close()

# 4) 生成 init.sql 种子片段（t_post + t_tag 两条 INSERT）
def sql_str(s):
    return "'" + s.replace("'", "''") + "'"

lines = []
lines.append("-- 博客文章：14 篇开发记录（slug = 年月日时分编号）")
lines.append("INSERT INTO `t_post` (`id`, `slug`, `post_date`, `title`, `excerpt`, `minutes`, `tags`, `content`) VALUES")
for i, p in enumerate(POSTS):
    vals = "(%d, %s, %s, %s, %s, %d, %s, %s)" % (
        i + 1,
        sql_str(p["slug"]), sql_str(p["date"]), sql_str(p["title"]),
        sql_str(p["excerpt"]), p["minutes"], sql_str(p["tags"]), sql_str(p["content"]),
    )
    lines.append(vals + ("," if i < len(POSTS) - 1 else ";"))
lines.append("")
lines.append("-- 标签云（由文章 tags 统计生成）")
tags_sorted = sorted(tag_count.items())
lines.append("INSERT INTO `t_tag` (`name`, `count`) VALUES")
lines.append(", ".join("(%s, %d)" % (sql_str(k), v) for k, v in tags_sorted) + ";")

frag = "\n".join(lines)
out = r"C:\Users\Dell\DoubaoWork\chats\2026-09-22\new-chat-1\leo-server\src\main\resources\sql\seed_posts_fragment.sql"
with open(out, "w", encoding="utf-8") as f:
    f.write(frag)
print("fragment written:", out, "chars:", len(frag))
