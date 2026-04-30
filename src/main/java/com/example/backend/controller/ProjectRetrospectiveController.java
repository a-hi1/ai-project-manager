package com.example.backend.controller;

import com.example.backend.entity.ProjectRetrospective;
import com.example.backend.service.ProjectRetrospectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-retrospective")
public class ProjectRetrospectiveController {
    @Autowired
    private ProjectRetrospectiveService projectRetrospectiveService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<ProjectRetrospective> retrospectives = projectRetrospectiveService.getByProjectId(projectId);
        return Map.of("success", true, "data", retrospectives);
    }

    @PostMapping("/generate/{projectId}")
    public Map<String, Object> generateRetrospective(@PathVariable Integer projectId, @RequestBody Map<String, Integer> data) {
        try {
            Integer createdBy = data.get("userId");
            ProjectRetrospective retrospective = projectRetrospectiveService.generateRetrospective(projectId, createdBy);
            return Map.of("success", true, "data", retrospective);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody ProjectRetrospective retrospective) {
        try {
            projectRetrospectiveService.updateById(retrospective);
            return Map.of("success", true, "data", retrospective);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 生成项目结束报告
     */
    @GetMapping("/report/{projectId}")
    public Map<String, Object> generateProjectEndReport(@PathVariable Integer projectId) {
        try {
            Map<String, Object> report = projectRetrospectiveService.generateProjectEndReport(projectId);
            return Map.of("success", true, "data", report);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("生成报告错误: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage(), "error", e.getClass().getName());
        }
    }

    /**
     * 导出项目结束报告为HTML
     */
    @GetMapping("/report/export/{projectId}")
    public ResponseEntity<byte[]> exportReportAsHtml(@PathVariable Integer projectId) {
        try {
            String html = projectRetrospectiveService.exportReportAsHtml(projectId);
            byte[] content = html.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDispositionFormData("attachment", "project-end-report.html");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }
}
