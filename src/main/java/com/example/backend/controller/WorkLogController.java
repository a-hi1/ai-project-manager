package com.example.backend.controller;

import com.example.backend.entity.WorkLog;
import com.example.backend.service.WorkLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-log")
public class WorkLogController {
    @Autowired
    private WorkLogService workLogService;

    @GetMapping("/task/{taskId}")
    public Map<String, Object> getLogsByTaskId(@PathVariable Integer taskId) {
        List<WorkLog> logs = workLogService.getLogsByTaskId(taskId);
        return Map.of("success", true, "data", logs);
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getLogsByUserId(@PathVariable Integer userId) {
        List<WorkLog> logs = workLogService.getLogsByUserId(userId);
        return Map.of("success", true, "data", logs);
    }

    @PostMapping("/create")
    public Map<String, Object> createLog(@RequestBody WorkLog log) {
        try {
            WorkLog created = workLogService.createLog(log);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}