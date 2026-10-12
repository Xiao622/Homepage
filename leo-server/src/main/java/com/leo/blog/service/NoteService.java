package com.leo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leo.blog.dto.NoteVO;
import com.leo.blog.entity.Note;
import com.leo.blog.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 随记服务：查询（时间 / 随机排序 + 关键词模糊搜索）、新建、更新、删除。
 * 写操作仅供编辑模式（管理员登录）调用，接口层负责鉴权。
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteMapper noteMapper;

    /**
     * 查询随记列表。
     *
     * @param order   time=按时间倒序（默认）；random=随机排序
     * @param keyword 可选，模糊匹配标题或正文（title LIKE / text LIKE）
     */
    public Map<String, Object> listNotes(String order, String keyword) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Note::getTitle, kw).or().like(Note::getText, kw));
        }
        List<Note> rows = noteMapper.selectList(wrapper);
        List<NoteVO> list;
        if ("random".equals(order)) {
            // 随机排序（MySQL RAND()）
            wrapper.last("ORDER BY RAND()");
            list = noteMapper.selectList(wrapper).stream()
                    .map(this::toVO)
                    .collect(Collectors.toList());
        } else {
            // 时间倒序：种子日期格式混杂（2026-9-17 与 09-11 并存），
            // 用「年份补全 + 月日补零」的语义键内存排序，新建随记（今天）自然置顶
            list = rows.stream()
                    .sorted((a, b) -> {
                        int c = sortKey(b.getDate()).compareTo(sortKey(a.getDate()));
                        return c != 0 ? c : Long.compare(a.getId(), b.getId());
                    })
                    .map(this::toVO)
                    .collect(Collectors.toList());
        }
        return Map.of("list", list, "total", list.size());
    }

    /** 日期 -> 可比较语义键（yyyyMMdd；无年份时按 2026 年补全）。 */
    private String sortKey(String date) {
        String d = date == null ? "" : date.trim();
        if (!d.matches("^\\d{4}-.*")) d = "2026-" + d;
        String[] p = d.split("-");
        if (p.length < 3) return d;
        try {
            return p[0]
                    + String.format("%02d", Integer.parseInt(p[1]))
                    + String.format("%02d", Integer.parseInt(p[2]));
        } catch (NumberFormatException e) {
            return d;
        }
    }

    /**
     * 新建随记（时间 / 标题 / 内容必填至少时间与内容）。
     */
    public NoteVO createNote(NoteVO vo) {
        Note note = new Note();
        fillNote(note, vo);
        note.setExpandable(false);
        noteMapper.insert(note);
        return toVO(note);
    }

    /**
     * 更新随记（按 id 定位）。
     * 不存在时抛出 IllegalArgumentException。
     */
    public NoteVO updateNote(Long id, NoteVO vo) {
        Note old = noteMapper.selectById(id);
        if (old == null) {
            throw new IllegalArgumentException("随记不存在");
        }
        Note note = new Note();
        note.setId(id);
        fillNote(note, vo);
        // expandable 保留原值（编辑不改变展开行为）
        note.setExpandable(old.getExpandable());
        noteMapper.updateById(note);
        return toVO(noteMapper.selectById(id));
    }

    /**
     * 删除随记（按 id 定位）。
     */
    public void deleteNote(Long id) {
        noteMapper.deleteById(id);
    }

    /** 把前端 VO 字段写入实体。 */
    private void fillNote(Note note, NoteVO vo) {
        note.setDate(vo.getDate());
        note.setTitle(StringUtils.hasText(vo.getTitle()) ? vo.getTitle().trim() : null);
        note.setText(vo.getText());
    }

    private NoteVO toVO(Note note) {
        NoteVO vo = new NoteVO();
        vo.setId(note.getId());
        vo.setDate(note.getDate());
        vo.setTitle(note.getTitle());
        vo.setText(note.getText());
        vo.setExpandable(note.getExpandable());
        return vo;
    }
}
