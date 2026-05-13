package com.example.backend.controller;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.ChangeRequest;
import com.example.backend.service.ChangeRequestService;
import com.example.backend.utils.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/change-request")
public class ChangeRequestController {
    @Autowired
    private ChangeRequestService changeRequestService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<ChangeRequest> requests = changeRequestService.getByProjectId(projectId);
        return Map.of("success", true, "data", requests);
    }

    @PostMapping("/create")
    @RequirePermission(PermissionUtils.PERM_CHANGE_MANAGE)
    public Map<String, Object> create(@RequestBody ChangeRequest changeRequest) {
        try {
            ChangeRequest created = changeRequestService.create(changeRequest);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    @RequirePermission(PermissionUtils.PERM_CHANGE_MANAGE)
    public Map<String, Object> update(@RequestBody ChangeRequest changeRequest) {
        try {
            changeRequestService.updateById(changeRequest);
            return Map.of("success", true, "data", changeRequest);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/approve/{id}")
    @RequirePermission(PermissionUtils.PERM_CHANGE_MANAGE)
    public Map<String, Object> approve(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            ChangeRequest changeRequest = changeRequestService.getById(id);
            if (changeRequest != null) {
                changeRequest.setStatus("approved");
                changeRequest.setReviewerId((Integer) data.get("reviewerId"));
                changeRequestService.updateById(changeRequest);
                return Map.of("success", true, "data", changeRequest);
            }
            return Map.of("success", false, "message", "变更请求不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/reject/{id}")
    @RequirePermission(PermissionUtils.PERM_CHANGE_MANAGE)
    public Map<String, Object> reject(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            ChangeRequest changeRequest = changeRequestService.getById(id);
            if (changeRequest != null) {
                changeRequest.setStatus("rejected");
                changeRequest.setReviewerId((Integer) data.get("reviewerId"));
                changeRequestService.updateById(changeRequest);
                return Map.of("success", true, "data", changeRequest);
            }
            return Map.of("success", false, "message", "变更请求不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
