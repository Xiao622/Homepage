package com.leo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leo.blog.common.PageResult;
import com.leo.blog.dto.PostVO;
import com.leo.blog.entity.Post;
import com.leo.blog.entity.Tag;
import com.leo.blog.mapper.PostMapper;
import com.leo.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章服务：列表分页、标签筛选、标签云、详情、增删改（写操作同步维护标签云计数）。
 * 路由编号 slug 规则：新建文章 = 年月日时分（yyyyMMddHHmm，冲突时递增后缀）；
 * 种子文章 = 年月日 + 4 位序号（如 202609160001）。
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    /**
     * 分页查询文章，支持按标签筛选（FIND_IN_SET）与标题模糊搜索（LIKE）。
     */
    public PageResult<PostVO> pagePosts(long page, long pageSize, String tag, String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tag)) {
            wrapper.apply("FIND_IN_SET({0}, tags)", tag.trim());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Post::getTitle, keyword.trim());
        }
        // 新文章在前（post_date 为统一 YYYY.MM.DD 格式，字符串倒序即时间倒序）
        wrapper.orderByDesc(Post::getDate).orderByDesc(Post::getId);

        Page<Post> p = postMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<PostVO> vos = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(vos, p.getTotal(), p.getCurrent(), p.getSize());
    }

    /**
     * 标签云（按文章数倒序）。
     */
    public List<Tag> listTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCount));
    }

    /**
     * 文章详情（按路由编号 slug 查询，含正文）。
     * 不存在时返回 null。
     */
    public PostVO getPostBySlug(String slug) {
        if (!StringUtils.hasText(slug)) return null;
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getSlug, slug.trim()));
        if (post == null) return null;
        PostVO vo = toVO(post);
        vo.setContent(post.getContent());
        return vo;
    }

    /**
     * 新建文章（自动生成路由编号；标签云计数 +1）。
     */
    public PostVO createPost(PostVO vo) {
        Post post = new Post();
        fillPost(post, vo);
        post.setSlug(genSlug());
        postMapper.insert(post);
        syncTagCounts(List.of(), parseTags(post.getTags()));
        return toVO(post);
    }

    /**
     * 更新文章（按 slug 定位；标签变化时同步标签云计数）。
     * 文章不存在时抛出 IllegalArgumentException。
     */
    public PostVO updatePost(String slug, PostVO vo) {
        Post old = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getSlug, slug.trim()));
        if (old == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        Post post = new Post();
        post.setId(old.getId());
        fillPost(post, vo);
        postMapper.updateById(post);
        syncTagCounts(parseTags(old.getTags()), parseTags(post.getTags()));
        PostVO updated = toVO(postMapper.selectById(old.getId()));
        updated.setContent(post.getContent());
        return updated;
    }

    /**
     * 保存博客正文图片（Ctrl+V 粘贴 / 上传），存 uploads/blog/，返回访问路径。
     * 图片格式白名单：jpg / jpeg / png / gif / webp / bmp / avif / svg。
     */
    private static final Pattern IMG_URL = Pattern.compile("/uploads/blog/[A-Za-z0-9._-]+");

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        String ext = "";
        String name = file.getOriginalFilename();
        if (StringUtils.hasText(name)) {
            int dot = name.lastIndexOf('.');
            if (dot >= 0) ext = name.substring(dot).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp|avif|svg)$")) {
            throw new IllegalArgumentException("不支持的图片格式：" + ext);
        }
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        try {
            Path dir = Paths.get("uploads").resolve("blog");
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败：" + e.getMessage(), e);
        }
        return "/uploads/blog/" + fileName;
    }

    /**
     * 删除文章时，连带清理正文里引用的 uploads/blog/ 图片文件，避免磁盘残留。
     */
    private void deletePostImages(String content) {
        if (!StringUtils.hasText(content)) return;
        Matcher m = IMG_URL.matcher(content);
        while (m.find()) {
            String path = m.group();
            try {
                Path file = Paths.get("uploads").resolve(path.substring("/uploads/".length())).toAbsolutePath();
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // 文件删除失败不阻塞文章删除
            }
        }
    }

    /**
     * 删除文章（按 slug 定位；标签云计数 -1，减到 0 的标签一并删除；
     * 正文引用的博客图片文件一并清理）。
     */
    public void deletePost(String slug) {
        Post old = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getSlug, slug.trim()));
        if (old == null) {
            return;
        }
        deletePostImages(old.getContent());
        postMapper.deleteById(old.getId());
        syncTagCounts(parseTags(old.getTags()), List.of());
    }

    /** 生成唯一路由编号：年月日时分（yyyyMMddHHmm），同分钟冲突时递增后缀。 */
    private String genSlug() {
        String base = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String slug = base;
        int suffix = 1;
        while (postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getSlug, slug)) > 0) {
            slug = base + (++suffix);
        }
        return slug;
    }

    /** 把前端 VO 字段写入实体（tags 数组 -> CSV）。 */
    private void fillPost(Post post, PostVO vo) {
        post.setTitle(vo.getTitle());
        post.setDate(vo.getDate());
        post.setExcerpt(vo.getExcerpt());
        post.setContent(vo.getContent());
        post.setMinutes(vo.getMinutes() == null ? 5 : vo.getMinutes());
        post.setTags(String.join(",", postTags(vo.getTags())));
    }

    /** 标签数组 -> trim + 去空 + 去重。 */
    private List<String> postTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    /** CSV -> 标签列表（trim + 去空 + 去重）。 */
    private List<String> parseTags(String csv) {
        if (!StringUtils.hasText(csv)) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 增量维护标签云：旧标签 -1、新标签 +1（新旧共有部分不动）。
     * 计数减到 0 的标签行删除；新标签首次出现则插入 count=1。
     */
    private void syncTagCounts(List<String> oldTags, List<String> newTags) {
        for (String t : oldTags) {
            if (!newTags.contains(t)) changeTagCount(t, -1);
        }
        for (String t : newTags) {
            if (!oldTags.contains(t)) changeTagCount(t, 1);
        }
    }

    private void changeTagCount(String name, int delta) {
        Tag tag = tagMapper.selectById(name);
        if (tag == null) {
            if (delta > 0) {
                Tag nt = new Tag();
                nt.setName(name);
                nt.setCount(Math.max(1, delta));
                tagMapper.insert(nt);
            }
            return;
        }
        int next = tag.getCount() + delta;
        if (next <= 0) {
            tagMapper.deleteById(name);
        } else {
            tag.setCount(next);
            tagMapper.updateById(tag);
        }
    }

    private PostVO toVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setSlug(post.getSlug());
        vo.setDate(post.getDate());
        vo.setTitle(post.getTitle());
        vo.setExcerpt(post.getExcerpt());
        vo.setMinutes(post.getMinutes());
        vo.setTags(StringUtils.hasText(post.getTags())
                ? Arrays.stream(post.getTags().split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList())
                : List.of());
        return vo;
    }
}
