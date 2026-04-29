package com.example.backend.controller;

import com.example.backend.entity.Deliverable;
import com.example.backend.service.DeliverableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliverable")
public class DeliverableController {
    @Autowired
    private DeliverableService deliverableService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<Deliverable> deliverables = deliverableService.getByProjectId(projectId);
        return Map.of("success", true, "data", deliverables);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Deliverable deliverable) {
        try {
            Deliverable created = deliverableService.create(deliverable);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Deliverable deliverable) {
        try {
            deliverableService.updateById(deliverable);
            return Map.of("success", true, "data", deliverable);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/approve/{id}")
    public Map<String, Object> approve(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            Deliverable deliverable = deliverableService.getById(id);
            if (deliverable != null) {
                deliverable.setStatus("approved");
                deliverable.setReviewedBy((Integer) data.get("reviewerId"));
                deliverableService.updateById(deliverable);
                return Map.of("success", true, "data", deliverable);
            }
            return Map.of("success", false, "message", "交付物不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/reject/{id}")
    public Map<String, Object> reject(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            Deliverable deliverable = deliverableService.getById(id);
            if (deliverable != null) {
                deliverable.setStatus("rejected");
                deliverable.setReviewedBy((Integer) data.get("reviewerId"));
                deliverableService.updateById(deliverable);
                return Map.of("success", true, "data", deliverable);
            }
            return Map.of("success", false, "message", "交付物不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}