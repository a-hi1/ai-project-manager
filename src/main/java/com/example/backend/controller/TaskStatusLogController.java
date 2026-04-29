package com.example.backend.controller;

import com.example.backend.entity.TaskStatusLog;
import com.example.backend.service.TaskStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task-status-log")
public class TaskStatusLogController {
    @Autowired
    private TaskStatusLogService taskStatusLogService;

    @GetMapping("/task/{taskId}")
    public Map<String, Object> getLogsByTaskId(@PathVariable Integer taskId) {
        List<TaskStatusLog> logs = taskStatusLogService.getLogsByTaskId(taskId);
        return Map.of("success", true, "data", logs);
    }

    @PostMapping("/create")
    public Map<String, Object> createLog(@RequestBody TaskStatusLog log) {
        try {
            TaskStatusLog created = taskStatusLogService.createLog(log);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}