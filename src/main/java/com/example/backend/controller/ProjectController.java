package com.example.backend.controller;

import com.example.backend.entity.PageResult;
import com.example.backend.entity.Project;
import com.example.backend.entity.ProjectMember;
import com.example.backend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    
    @PostMapping("/create")
    public Map<String, Object> createProject(@RequestBody Project project) {
        try {
            Project createdProject = projectService.createProject(project);
            return Map.of("success", true, "data", createdProject);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public Map<String, Object> getProjectList() {
        PageResult<Project> pageResult = projectService.list();
        return Map.of("success", true, "data", pageResult.getRecords(), "total", pageResult.getTotal());
    }
    
    @GetMapping("/get/{id}")
    public Map<String, Object> getProjectById(@PathVariable Integer id) {
        Project project = projectService.getById(id);
        if (project != null) {
            return Map.of("success", true, "data", project);
        } else {
            return Map.of("success", false, "message", "项目不存在");
        }
    }
    
    @PutMapping("/update")
    public Map<String, Object> updateProject(@RequestBody Project project) {
        try {
            Project existingProject = projectService.getById(project.getId());
            if (existingProject == null) {
                return Map.of("success", false, "message", "项目不存在");
            }
            if (project.getName() != null) existingProject.setName(project.getName());
            if (project.getDescription() != null) existingProject.setDescription(project.getDescription());
            if (project.getStartDate() != null) existingProject.setStartDate(project.getStartDate());
            if (project.getEndDate() != null) existingProject.setEndDate(project.getEndDate());
            if (project.getStatus() != null) existingProject.setStatus(project.getStatus());
            
            projectService.updateById(existingProject);
            return Map.of("success", true, "data", existingProject);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteProject(@PathVariable Integer id) {
        try {
            projectService.removeById(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @PostMapping("/member/add")
    public Map<String, Object> addProjectMember(@RequestBody ProjectMember projectMember) {
        try {
            projectService.addProjectMember(projectMember);
            return Map.of("success", true, "data", projectMember);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @DeleteMapping("/member/remove")
    public Map<String, Object> removeProjectMember(@RequestParam Integer projectId, @RequestParam Integer userId) {
        try {
            projectService.removeProjectMember(projectId, userId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @GetMapping("/member/list/{projectId}")
    public Map<String, Object> getProjectMembers(@PathVariable Integer projectId) {
        List<ProjectMember> members = projectService.getProjectMembers(projectId);
        return Map.of("success", true, "data", members);
    }
    
    @GetMapping("/user/{userId}")
    public Map<String, Object> getProjectsByUserId(@PathVariable Integer userId) {
        List<Project> projects = projectService.getProjectsByUserId(userId);
        return Map.of("success", true, "data", projects);
    }

    @GetMapping("/stats/{projectId}")
    public Map<String, Object> getProjectStats(@PathVariable Integer projectId) {
        Object stats = projectService.getProjectStats(projectId);
        return Map.of("success", true, "data", stats);
    }
}