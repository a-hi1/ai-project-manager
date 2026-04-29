package com.example.backend.controller;

import com.example.backend.entity.ProjectRetrospective;
import com.example.backend.service.ProjectRetrospectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody ProjectRetrospective retrospective) {
        try {
            projectRetrospectiveService.updateById(retrospective);
            return Map.of("success", true, "data", retrospective);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}