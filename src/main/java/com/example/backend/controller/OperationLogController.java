package com.example.backend.controller;

import com.example.backend.entity.OperationLog;
import com.example.backend.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/log")
public class OperationLogController {
    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Map<String, Object> getRecentLogs() {
        try {
            List<OperationLog> logs = operationLogService.getRecentLogs();
            return Map.of("success", true, "data", logs);
        } catch (Exception e) {
            return Map.of("success", true, "data", List.of());
        }
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getLogsByUserId(@PathVariable Integer userId) {
        try {
            List<OperationLog> logs = operationLogService.getLogsByUserId(userId);
            return Map.of("success", true, "data", logs);
        } catch (Exception e) {
            return Map.of("success", true, "data", List.of());
        }
    }
}
