package com.example.backend.controller;

import com.example.backend.entity.Milestone;
import com.example.backend.service.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/milestone")
public class MilestoneController {
    @Autowired
    private MilestoneService milestoneService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getMilestonesByProjectId(@PathVariable Integer projectId) {
        List<Milestone> milestones = milestoneService.getMilestonesByProjectId(projectId);
        return Map.of("success", true, "data", milestones);
    }

    @PostMapping("/create")
    public Map<String, Object> createMilestone(@RequestBody Milestone milestone) {
        try {
            Milestone createdMilestone = milestoneService.createMilestone(milestone);
            return Map.of("success", true, "data", createdMilestone);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> updateMilestone(@RequestBody Milestone milestone) {
        try {
            milestoneService.updateById(milestone);
            return Map.of("success", true, "data", milestone);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteMilestone(@PathVariable Integer id) {
        try {
            milestoneService.removeById(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}