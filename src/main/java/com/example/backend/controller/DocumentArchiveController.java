package com.example.backend.controller;

import com.example.backend.entity.DocumentArchive;
import com.example.backend.service.DocumentArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/document-archive")
public class DocumentArchiveController {
    @Autowired
    private DocumentArchiveService documentArchiveService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<DocumentArchive> archives = documentArchiveService.getByProjectId(projectId);
        return Map.of("success", true, "data", archives);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody DocumentArchive documentArchive) {
        try {
            DocumentArchive created = documentArchiveService.create(documentArchive);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/archive-project/{projectId}")
    public Map<String, Object> archiveProject(@PathVariable Integer projectId, @RequestBody Map<String, Integer> data) {
        try {
            Integer archivedBy = data.get("userId");
            documentArchiveService.archiveProjectDocuments(projectId, archivedBy);
            return Map.of("success", true, "message", "项目文档归档成功");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        try {
            boolean deleted = documentArchiveService.removeById(id);
            if (deleted) {
                return Map.of("success", true, "message", "删除成功");
            } else {
                return Map.of("success", false, "message", "文档不存在");
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/project/{projectId}")
    public Map<String, Object> deleteByProjectId(@PathVariable Integer projectId) {
        try {
            documentArchiveService.deleteByProjectId(projectId);
            return Map.of("success", true, "message", "项目文档归档已清空");
        } catch (Exception e) {
            return Map.of("success", false, "message", "清空失败: " + e.getMessage());
        }
    }
}