package com.example.backend.controller;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.PageResult;
import com.example.backend.entity.Task;
import com.example.backend.entity.TaskDependency;
import com.example.backend.service.TaskService;
import com.example.backend.utils.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getTasksByProjectId(@PathVariable Integer projectId) {
        List<Task> tasks = taskService.getTasksByProjectIdList(projectId);
        return Map.of("success", true, "data", tasks);
    }

    @GetMapping("/project/{projectId}/page")
    public Map<String, Object> getTasksByProjectIdWithPage(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<Task> result = taskService.getTasksByProjectId(projectId, page, pageSize);
        return Map.of("success", true, "data", result.getRecords(),
                      "total", result.getTotal(), "page", result.getPage(),
                      "pageSize", result.getPageSize(), "totalPages", result.getTotalPages());
    }

    @GetMapping("/stats/{projectId}")
    public Map<String, Object> getTaskStats(@PathVariable Integer projectId) {
        TaskService.TaskStats stats = taskService.getTaskStatsByProjectId(projectId);
        return Map.of("success", true, "data", stats);
    }

    @GetMapping("/parent/{parentId}")
    public Map<String, Object> getChildTasks(@PathVariable Integer parentId) {
        List<Task> tasks = taskService.getChildTasks(parentId);
        return Map.of("success", true, "data", tasks);
    }

    @GetMapping("/milestone/{projectId}")
    public Map<String, Object> getMilestones(@PathVariable Integer projectId) {
        List<Task> milestones = taskService.getMilestonesByProjectId(projectId);
        return Map.of("success", true, "data", milestones);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getTaskById(@PathVariable Integer id) {
        Task task = taskService.getById(id);
        if (task != null) {
            return Map.of("success", true, "data", task);
        }
        return Map.of("success", false, "message", "Task not found");
    }

    @PostMapping("/create")
    @RequirePermission(PermissionUtils.PERM_TASK_CREATE)
    public Map<String, Object> createTask(@RequestBody Task task) {
        try {
            Task createdTask = taskService.createTask(task);
            return Map.of("success", true, "data", createdTask);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/update")
    @RequirePermission(PermissionUtils.PERM_TASK_MANAGE)
    public Map<String, Object> updateTask(@RequestBody Task task) {
        try {
            System.out.println("更新任务，收到数据 id: " + task.getId() + ", status: " + task.getStatus());
            Task updatedTask = taskService.updateTask(task);
            System.out.println("任务更新成功");
            return Map.of("success", true, "data", updatedTask);
        } catch (Exception e) {
            System.err.println("任务更新失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission(PermissionUtils.PERM_TASK_MANAGE)
    public Map<String, Object> deleteTask(@PathVariable Integer id) {
        try {
            taskService.removeById(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/dependency/add")
    @RequirePermission(PermissionUtils.PERM_TASK_MANAGE)
    public Map<String, Object> addDependency(@RequestBody TaskDependency dependency) {
        try {
            TaskDependency created = taskService.addDependency(dependency);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/dependency/{id}")
    @RequirePermission(PermissionUtils.PERM_TASK_MANAGE)
    public Map<String, Object> removeDependency(@PathVariable Integer id) {
        try {
            taskService.removeDependency(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/dependency/{taskId}")
    public Map<String, Object> getDependencies(@PathVariable Integer taskId) {
        List<TaskDependency> dependencies = taskService.getDependenciesByTaskId(taskId);
        return Map.of("success", true, "data", dependencies);
    }
}
