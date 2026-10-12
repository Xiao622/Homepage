package com.leo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leo.blog.dto.TrackVO;
import com.leo.blog.entity.Track;
import com.leo.blog.mapper.TrackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 音乐服务：曲目列表查询（公开）、上传（音频 + 封面，需登录）、删除（需登录）。
 * 文件存放：uploads/music/（音频）、uploads/music/covers/（封面）。
 * 上传的音频格式白名单：mp3 / wav / ogg / m4a / flac / aac / webm。
 * 前端 Music 页数据源为静态快照 ../zyyo-vue/public/music-tracks.json，
 * 上传/删除成功后自动重写该快照，保证页面列表与数据库一致。
 */
@Service
@RequiredArgsConstructor
public class MusicService {

    private static final String AUDIO_EXT = "\\.(mp3|wav|ogg|m4a|flac|aac|webm)$";
    private static final String IMG_EXT = "\\.(jpg|jpeg|png|gif|webp|bmp|avif|svg)$";
    /** 前端 Vite 静态目录中的曲目快照（页面数据源） */
    private static final String SNAPSHOT_PATH = "../zyyo-vue/public/music-tracks.json";

    private final TrackMapper trackMapper;
    private final ObjectMapper objectMapper;

    /**
     * 曲目列表（按 id 倒序，最新上传的曲目排在最前，主推区展示最新 3 首）。
     */
    public List<TrackVO> listTracks() {
        return trackMapper.selectList(
                        new LambdaQueryWrapper<Track>().orderByDesc(Track::getId))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 上传曲目：音频必传（存 uploads/music/），封面可选（存 uploads/music/covers/）。
     * title / artist 必填，enTitle / duration 可选（duration 缺省 180）。
     */
    public TrackVO uploadTrack(MultipartFile file, MultipartFile cover,
                               String title, String enTitle, String artist, Integer duration) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的音频文件");
        }
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("请填写歌曲名称");
        }
        if (!StringUtils.hasText(artist)) {
            throw new IllegalArgumentException("请填写演唱者");
        }

        String audioPath = saveFile(file, "music", AUDIO_EXT, "音频");
        String coverPath = null;
        if (cover != null && !cover.isEmpty()) {
            coverPath = saveFile(cover, "music/covers", IMG_EXT, "封面图片");
        }

        Track track = new Track();
        track.setTitle(title.trim());
        track.setEnTitle(StringUtils.hasText(enTitle) ? enTitle.trim() : null);
        track.setArtist(artist.trim());
        track.setDuration(duration != null && duration > 0 ? duration : 180);
        track.setCoverSeed((int) (Math.random() * 8)); // 无封面时前端渐变占位
        track.setUrl(audioPath);
        track.setCover(coverPath);
        trackMapper.insert(track);
        writeSnapshot();
        return toVO(track);
    }

    /**
     * 删除曲目：同时删除磁盘上的音频与封面文件。
     */
    public void deleteTrack(Long id) {
        Track old = trackMapper.selectById(id);
        if (old == null) {
            return;
        }
        trackMapper.deleteById(id);
        deleteFile(old.getUrl());
        deleteFile(old.getCover());
        writeSnapshot();
    }

    /** 把最新曲目列表写入前端静态快照（Music 页数据源），失败不阻塞主流程。 */
    private void writeSnapshot() {
        try {
            List<TrackVO> list = listTracks();
            Map<String, Object> body = new HashMap<>();
            body.put("code", 0);
            body.put("message", "ok");
            body.put("data", list);
            Path p = Paths.get(SNAPSHOT_PATH).toAbsolutePath();
            Files.write(p, objectMapper.writeValueAsBytes(body));
        } catch (IOException e) {
            // 快照写入失败不影响上传/删除主流程
        }
    }

    /** 保存上传文件到 uploads/<subDir>/，UUID 文件名保留扩展名，返回访问路径。 */
    private String saveFile(MultipartFile file, String subDir, String extPattern, String label) {
        String ext = "";
        String name = file.getOriginalFilename();
        if (StringUtils.hasText(name)) {
            int dot = name.lastIndexOf('.');
            if (dot >= 0) ext = name.substring(dot).toLowerCase();
        }
        if (!ext.matches(extPattern)) {
            throw new IllegalArgumentException("不支持的" + label + "格式：" + ext);
        }
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        try {
            Path dir = Paths.get("uploads").resolve(subDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(label + "保存失败：" + e.getMessage(), e);
        }
        return "/uploads/" + subDir + "/" + fileName;
    }

    private void deleteFile(String path) {
        if (StringUtils.hasText(path) && path.startsWith("/uploads/")) {
            try {
                Path file = Paths.get("uploads").resolve(path.substring("/uploads/".length())).toAbsolutePath();
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // 文件删除失败不阻塞数据删除
            }
        }
    }

    private TrackVO toVO(Track t) {
        TrackVO vo = new TrackVO();
        vo.setId(t.getId());
        vo.setTitle(t.getTitle());
        vo.setEnTitle(t.getEnTitle());
        vo.setArtist(t.getArtist());
        vo.setDuration(t.getDuration());
        vo.setCoverSeed(t.getCoverSeed());
        vo.setUrl(t.getUrl());
        vo.setCover(t.getCover());
        return vo;
    }
}
