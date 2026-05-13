package com.example.backend.controller;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.Bug;
import com.example.backend.service.BugService;
import com.example.backend.utils.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bug")
public class BugController {
    @Autowired
    private BugService bugService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<Bug> bugs = bugService.getByProjectId(projectId);
        return Map.of("success", true, "data", bugs);
    }

    @GetMapping("/assignee/{userId}")
    public Map<String, Object> getByAssigneeId(@PathVariable Integer userId) {
        List<Bug> bugs = bugService.getByAssigneeId(userId);
        return Map.of("success", true, "data", bugs);
    }

    @PostMapping("/create")
    @RequirePermission(PermissionUtils.PERM_BUG_MANAGE)
    public Map<String, Object> create(@RequestBody Bug bug) {
        try {
            Bug created = bugService.create(bug);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    @RequirePermission(PermissionUtils.PERM_BUG_MANAGE)
    public Map<String, Object> update(@RequestBody Bug bug) {
        try {
            bugService.updateById(bug);
            return Map.of("success", true, "data", bug);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/assign/{id}")
    @RequirePermission(PermissionUtils.PERM_BUG_MANAGE)
    public Map<String, Object> assign(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            Bug bug = bugService.getById(id);
            if (bug != null) {
                bug.setAssigneeId((Integer) data.get("assigneeId"));
                bugService.updateById(bug);
                return Map.of("success", true, "data", bug);
            }
            return Map.of("success", false, "message", "Bug不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/resolve/{id}")
    @RequirePermission(PermissionUtils.PERM_BUG_MANAGE)
    public Map<String, Object> resolve(@PathVariable Integer id) {
        try {
            Bug bug = bugService.getById(id);
            if (bug != null) {
                bug.setStatus("resolved");
                bugService.updateById(bug);
                return Map.of("success", true, "data", bug);
            }
            return Map.of("success", false, "message", "Bug不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
