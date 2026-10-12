package com.leo.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leo.blog.entity.Track;
import org.apache.ibatis.annotations.Mapper;

/** 音乐曲目表 Mapper。 */
@Mapper
public interface TrackMapper extends BaseMapper<Track> {
}
