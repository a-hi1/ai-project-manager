package com.example.backend.controller;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.Risk;
import com.example.backend.service.RiskService;
import com.example.backend.utils.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskController {
    @Autowired
    private RiskService riskService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getRisksByProjectId(@PathVariable Integer projectId) {
        List<Risk> risks = riskService.getRisksByProjectId(projectId);
        return Map.of("success", true, "data", risks);
    }

    @PostMapping("/create")
    @RequirePermission(PermissionUtils.PERM_RISK_MANAGE)
    public Map<String, Object> createRisk(@RequestBody Risk risk) {
        try {
            Risk createdRisk = riskService.createRisk(risk);
            return Map.of("success", true, "data", createdRisk);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    @RequirePermission(PermissionUtils.PERM_RISK_MANAGE)
    public Map<String, Object> updateRisk(@RequestBody Risk risk) {
        try {
            riskService.updateById(risk);
            return Map.of("success", true, "data", risk);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission(PermissionUtils.PERM_RISK_MANAGE)
    public Map<String, Object> deleteRisk(@PathVariable Integer id) {
        try {
            riskService.removeById(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
