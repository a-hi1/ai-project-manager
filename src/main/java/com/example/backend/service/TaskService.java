package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.PageResult;
import com.example.backend.entity.Task;
import com.example.backend.entity.TaskDependency;
import com.example.backend.mapper.TaskDependencyMapper;
import com.example.backend.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {
    @Autowired
    private TaskDependencyMapper taskDependencyMapper;

    public Task getById(Integer id) {
        return super.getById(id);
    }

    public PageResult<Task> getTasksByProjectId(Integer projectId, int page, int pageSize) {
        Page<Task> pagination = new Page<>(page, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        wrapper.orderByAsc(Task::getLevel).orderByAsc(Task::getStartDate);
        IPage<Task> result = baseMapper.selectPage(pagination, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public PageResult<Task> getTasksByProjectId(Integer projectId) {
        return getTasksByProjectId(projectId, 1, 50);
    }

    public List<Task> getTasksByProjectIdList(Integer projectId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        wrapper.orderByAsc(Task::getLevel).orderByAsc(Task::getStartDate);
        return baseMapper.selectList(wrapper);
    }

    public List<Task> getChildTasks(Integer parentId) {
        return baseMapper.findByParentId(parentId);
    }

    public List<Task> getMilestonesByProjectId(Integer projectId) {
        return baseMapper.findMilestonesByProjectId(projectId);
    }

    @Transactional
    public Task createTask(Task task) {
        if (task.getParentId() != null) {
            Task parentTask = getById(task.getParentId());
            if (parentTask != null) {
                task.setLevel(parentTask.getLevel() + 1);
                task.setPath(parentTask.getPath() + "/" + UUID.randomUUID().toString());
            }
        } else {
            task.setLevel(0);
            task.setPath("/" + UUID.randomUUID().toString());
        }
        save(task);
        return task;
    }

    @Transactional
    public Task updateTask(Task task) {
        updateById(task);
        return task;
    }

    public void removeById(Integer id) {
        super.removeById(id);
    }

    @Transactional
    public TaskDependency addDependency(TaskDependency dependency) {
        taskDependencyMapper.insert(dependency);
        return dependency;
    }

    public void removeDependency(Integer id) {
        taskDependencyMapper.deleteById(id);
    }

    public List<TaskDependency> getDependenciesByTaskId(Integer taskId) {
        return taskDependencyMapper.findByTaskId(taskId);
    }

    public List<TaskDependency> getDependentTasks(Integer taskId) {
        return taskDependencyMapper.findByDependsOnId(taskId);
    }

    public TaskStats getTaskStatsByProjectId(Integer projectId) {
        List<Task> allTasks = getTasksByProjectIdList(projectId);
        int total = allTasks.size();
        int completed = (int) allTasks.stream().filter(t -> "done".equals(t.getStatus())).count();
        int inProgress = (int) allTasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
        int todo = (int) allTasks.stream().filter(t -> "todo".equals(t.getStatus())).count();
        return new TaskStats(total, completed, inProgress, todo);
    }

    public static class TaskStats {
        private int total;
        private int completed;
        private int inProgress;
        private int todo;

        public TaskStats(int total, int completed, int inProgress, int todo) {
            this.total = total;
            this.completed = completed;
            this.inProgress = inProgress;
            this.todo = todo;
        }

        public int getTotal() { return total; }
        public int getCompleted() { return completed; }
        public int getInProgress() { return inProgress; }
        public int getTodo() { return todo; }
        public double getCompletionRate() { return total > 0 ? (completed * 100.0 / total) : 0; }
    }
}
