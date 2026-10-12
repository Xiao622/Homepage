package com.leo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leo.blog.dto.PhotoVO;
import com.leo.blog.entity.Photo;
import com.leo.blog.entity.PhotoComment;
import com.leo.blog.mapper.PhotoMapper;
import com.leo.blog.mapper.PhotoCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 相册照片服务：相册列表、照片筛选（分类 / 年月）、上传（存本地 uploads/photos 目录）、
 * 更新信息、删除、照片评论。
 * 写操作仅供编辑模式（管理员登录）调用，接口层负责鉴权；评论对访客开放。
 */
@Service
@RequiredArgsConstructor
public class PhotoService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PhotoMapper photoMapper;
    private final PhotoCommentMapper photoCommentMapper;

    /**
     * 照片列表。
     *
     * @param category 分类名模糊匹配（可为空）
     * @param month    年月筛选（如 2026-09，匹配 taken_date 前缀；可为空）
     * @param limit    最多返回条数（可为空；首页取最近 N 张时使用）
     */
    public Map<String, Object> listPhotos(String category, String month, Integer limit) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.like(Photo::getCategory, category.trim());
        }
        if (StringUtils.hasText(month)) {
            wrapper.likeRight(Photo::getTakenDate, month.trim());
        }
        wrapper.orderByDesc(Photo::getId);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        List<PhotoVO> list = photoMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Map.of("list", list, "total", list.size());
    }

    /**
     * 分类统计（按照片数倒序），供筛选区展示。
     */
    public List<Map<String, Object>> listCategories() {
        return photoMapper.selectList(
                        new LambdaQueryWrapper<Photo>().select(Photo::getCategory))
                .stream()
                .map(Photo::getCategory)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
                .entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "name", e.getKey(),
                        "count", e.getValue().intValue()))
                .sorted((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")))
                .collect(Collectors.toList());
    }

    /**
     * 相册列表：按分类分组，每相册返回名称 / 照片数 / 封面（最新一张）。
     */
    public List<Map<String, Object>> listAlbums() {
        List<Photo> all = photoMapper.selectList(
                new LambdaQueryWrapper<Photo>().select(Photo::getId, Photo::getCategory, Photo::getUrl));
        return all.stream()
                .filter(p -> StringUtils.hasText(p.getCategory()))
                .collect(Collectors.groupingBy(Photo::getCategory, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    List<Photo> group = e.getValue();
                    group.sort(Comparator.comparing(Photo::getId).reversed());
                    return Map.<String, Object>of(
                            "name", e.getKey(),
                            "count", group.size(),
                            "cover", group.get(0).getUrl());
                })
                .sorted(Comparator.comparingInt((Map<String, Object> a) -> (Integer) a.get("count")).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 照片评论列表（按时间正序）。
     */
    public List<Map<String, Object>> listComments(Long photoId) {
        return photoCommentMapper.selectList(
                        new LambdaQueryWrapper<PhotoComment>()
                                .eq(PhotoComment::getPhotoId, photoId)
                                .orderByAsc(PhotoComment::getId))
                .stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "author", c.getAuthor(),
                        "content", c.getContent(),
                        "createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().format(TIME_FMT)))
                .collect(Collectors.toList());
    }

    /**
     * 发表照片评论（访客开放）。昵称与内容必填，照片不存在 404。
     */
    public Map<String, Object> addComment(Long photoId, String author, String content) {
        if (photoMapper.selectById(photoId) == null) {
            throw new IllegalArgumentException("照片不存在");
        }
        if (!StringUtils.hasText(author)) {
            throw new IllegalArgumentException("请填写昵称");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("请填写评论内容");
        }
        PhotoComment c = new PhotoComment();
        c.setPhotoId(photoId);
        c.setAuthor(author.trim());
        c.setContent(content.trim());
        photoCommentMapper.insert(c);
        return Map.of(
                "id", c.getId(),
                "author", c.getAuthor(),
                "content", c.getContent(),
                "createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().format(TIME_FMT));
    }

    /**
     * 上传照片：文件保存到 uploads/photos/ 目录（UUID 文件名保留扩展名），返回照片对象。
     */
    public PhotoVO uploadPhoto(MultipartFile file, String category, String takenDate, String caption) {
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
            Path dir = Paths.get("uploads").resolve("photos");
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败：" + e.getMessage(), e);
        }

        Photo photo = new Photo();
        photo.setUrl("/uploads/photos/" + fileName);
        photo.setCategory(StringUtils.hasText(category) ? category.trim() : "未分类");
        photo.setTakenDate(StringUtils.hasText(takenDate) ? takenDate.trim() : null);
        photo.setCaption(StringUtils.hasText(caption) ? caption.trim() : null);
        photoMapper.insert(photo);
        return toVO(photo);
    }

    /**
     * 更新照片信息（分类 / 拍摄时间 / 说明）。
     * 传 null 表示不修改该字段；传空串表示清空该字段。
     */
    public PhotoVO updatePhoto(Long id, PhotoVO vo) {
        Photo old = photoMapper.selectById(id);
        if (old == null) {
            throw new IllegalArgumentException("照片不存在");
        }
        Photo photo = new Photo();
        photo.setId(id);
        photo.setCategory(vo.getCategory() == null ? old.getCategory() : (StringUtils.hasText(vo.getCategory()) ? vo.getCategory().trim() : old.getCategory()));
        photo.setTakenDate(vo.getTakenDate() == null ? old.getTakenDate() : vo.getTakenDate().trim());
        photo.setCaption(vo.getCaption() == null ? old.getCaption() : vo.getCaption().trim());
        photoMapper.updateById(photo);
        return toVO(photoMapper.selectById(id));
    }

    /**
     * 删除照片：同时删除磁盘文件与该照片的评论。
     */
    public void deletePhoto(Long id) {
        Photo old = photoMapper.selectById(id);
        if (old == null) {
            return;
        }
        photoMapper.deleteById(id);
        photoCommentMapper.delete(new LambdaQueryWrapper<PhotoComment>().eq(PhotoComment::getPhotoId, id));
        if (StringUtils.hasText(old.getUrl()) && old.getUrl().startsWith("/uploads/")) {
            try {
                Path file = Paths.get("uploads").resolve(old.getUrl().substring("/uploads/".length())).toAbsolutePath();
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // 文件删除失败不阻塞数据删除
            }
        }
    }

    private PhotoVO toVO(Photo photo) {
        PhotoVO vo = new PhotoVO();
        vo.setId(photo.getId());
        vo.setUrl(photo.getUrl());
        vo.setCategory(photo.getCategory());
        vo.setTakenDate(photo.getTakenDate());
        vo.setCaption(photo.getCaption());
        return vo;
    }
}
