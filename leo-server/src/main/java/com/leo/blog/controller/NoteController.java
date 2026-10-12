package com.leo.blog.controller;

import com.leo.blog.common.Result;
import com.leo.blog.common.TokenStore;
import com.leo.blog.dto.NoteVO;
import com.leo.blog.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 随记接口：
 *  - 查询（公开）：按时间 / 随机排序，支持 keyword 模糊搜索
 *  - 写操作（需登录 token）：新建 / 按 id 更新 / 删除
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final TokenStore tokenStore;

    /** GET /api/notes?order=time|random&keyword=xxx */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "time") String order,
            @RequestParam(required = false) String keyword) {
        return Result.ok(noteService.listNotes(order, keyword));
    }

    /** POST /api/notes —— 新建随记（需登录） */
    @PostMapping
    public Result<NoteVO> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody(required = false) NoteVO body) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        if (body == null || !StringUtils.hasText(body.getDate()) || !StringUtils.hasText(body.getText())) {
            return Result.error(400, "时间和内容不能为空");
        }
        return Result.ok(noteService.createNote(body));
    }

    /** PUT /api/notes/{id} —— 更新随记（需登录） */
    @PutMapping("/{id}")
    public Result<NoteVO> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @RequestBody(required = false) NoteVO body) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        if (body == null || !StringUtils.hasText(body.getDate()) || !StringUtils.hasText(body.getText())) {
            return Result.error(400, "时间和内容不能为空");
        }
        try {
            return Result.ok(noteService.updateNote(id, body));
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /** DELETE /api/notes/{id} —— 删除随记（需登录） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Result<Void> authCheck = requireAdmin(auth);
        if (authCheck != null) {
            return Result.error(authCheck.getCode(), authCheck.getMessage());
        }
        noteService.deleteNote(id);
        return Result.ok();
    }

    /** 登录态校验：无效时返回 401 Result，有效返回 null。 */
    private Result<Void> requireAdmin(String auth) {
        if (auth == null || !auth.startsWith("Bearer ") || !tokenStore.valid(auth.substring(7))) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        return null;
    }
}
