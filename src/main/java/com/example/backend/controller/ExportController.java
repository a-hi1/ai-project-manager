package com.example.backend.controller;

import com.example.backend.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    @Autowired
    private ExportService exportService;

    @GetMapping("/tasks/{projectId}")
    public void exportTasks(@PathVariable Integer projectId, HttpServletResponse response) {
        try {
            exportService.exportTasks(projectId, response);
        } catch (IOException e) {
            throw new RuntimeException("导出任务列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/projects")
    public void exportProjects(HttpServletResponse response) {
        try {
            exportService.exportProjects(response);
        } catch (IOException e) {
            throw new RuntimeException("导出项目报表失败：" + e.getMessage());
        }
    }

    @GetMapping("/retrospective/{projectId}")
    public void exportRetrospective(@PathVariable Integer projectId, HttpServletResponse response) {
        try {
            exportService.exportRetrospective(projectId, response);
        } catch (IOException e) {
            throw new RuntimeException("导出复盘报告失败：" + e.getMessage());
        }
    }
}