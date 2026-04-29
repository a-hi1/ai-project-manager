package com.example.backend.controller;

import com.example.backend.entity.FeasibilityAnalysis;
import com.example.backend.service.FeasibilityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feasibility-analysis")
public class FeasibilityAnalysisController {
    @Autowired
    private FeasibilityAnalysisService feasibilityAnalysisService;
    
    @PostMapping("/create")
    public Map<String, Object> createFeasibilityAnalysis(@RequestBody FeasibilityAnalysis feasibilityAnalysis) {
        try {
            FeasibilityAnalysis createdAnalysis = feasibilityAnalysisService.createFeasibilityAnalysis(feasibilityAnalysis);
            return Map.of("success", true, "data", createdAnalysis);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<FeasibilityAnalysis> analyses = feasibilityAnalysisService.getByProjectId(projectId);
        return Map.of("success", true, "data", analyses);
    }
    
    @PutMapping("/update")
    public Map<String, Object> updateFeasibilityAnalysis(@RequestBody FeasibilityAnalysis feasibilityAnalysis) {
        try {
            feasibilityAnalysisService.updateById(feasibilityAnalysis);
            return Map.of("success", true, "data", feasibilityAnalysis);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteFeasibilityAnalysis(@PathVariable Integer id) {
        try {
            feasibilityAnalysisService.removeById(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}