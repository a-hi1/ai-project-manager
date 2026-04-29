package com.example.backend.controller;

import com.example.backend.entity.RequirementResearch;
import com.example.backend.service.RequirementResearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirement-research")
public class RequirementResearchController {
    @Autowired
    private RequirementResearchService requirementResearchService;
    
    @PostMapping("/create")
    public Map<String, Object> createRequirementResearch(@RequestBody RequirementResearch requirementResearch) {
        try {
            RequirementResearch createdResearch = requirementResearchService.createRequirementResearch(requirementResearch);
            return Map.of("success", true, "data", createdResearch);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<RequirementResearch> researches = requirementResearchService.getByProjectId(projectId);
        return Map.of("success", true, "data", researches);
    }
    
    @PutMapping("/update")
    public Map<String, Object> updateRequirementResearch(@RequestBody RequirementResearch requirementResearch) {
        try {
            requirementResearchService.updateById(requirementResearch);
            return Map.of("success", true, "data", requirementResearch);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteRequirementResearch(@PathVariable Integer id) {
        try {
            requirementResearchService.removeById(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}