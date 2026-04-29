package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.PageResult;
import com.example.backend.entity.Project;
import com.example.backend.entity.ProjectMember;
import com.example.backend.mapper.ProjectMapper;
import com.example.backend.mapper.ProjectMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    public Project selectById(Integer id) {
        return projectMapper.selectById(id);
    }

    public List<Project> findAll() {
        return projectMapper.findAll();
    }

    public PageResult<Project> list(int page, int pageSize) {
        Page<Project> pagination = new Page<>(page, pageSize);
        IPage<Project> result = projectMapper.selectPage(pagination);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public PageResult<Project> list() {
        return list(1, 20);
    }

    public Project getById(Integer id) {
        return selectById(id);
    }

    public Project createProject(Project project) {
        if (project.getCreatedAt() == null) {
            project.setCreatedAt(LocalDateTime.now());
        }
        if (project.getUpdatedAt() == null) {
            project.setUpdatedAt(LocalDateTime.now());
        }
        if (project.getCreatedBy() == null) {
            project.setCreatedBy(1);
        }
        projectMapper.insert(project);
        return project;
    }

    public void updateById(Project project) {
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(project);
    }

    public void removeById(Integer id) {
        projectMapper.deleteById(id);
    }

    public List<ProjectMember> getProjectMembers(Integer projectId) {
        return projectMemberMapper.findByProjectId(projectId);
    }

    public List<Project> getProjectsByUserId(Integer userId) {
        List<ProjectMember> projectMembers = projectMemberMapper.findByUserId(userId);
        return projectMembers.stream()
            .map(member -> projectMapper.selectById(member.getProjectId()))
            .collect(java.util.stream.Collectors.toList());
    }

    public ProjectStats getProjectStats(Integer projectId) {
        int taskCount = projectMapper.countTasksByProjectId(projectId);
        int completedTaskCount = projectMapper.countCompletedTasksByProjectId(projectId);
        int riskCount = projectMapper.countRisksByProjectId(projectId);
        int bugCount = projectMapper.countBugsByProjectId(projectId);

        return new ProjectStats(taskCount, completedTaskCount, riskCount, bugCount);
    }

    public void addProjectMember(ProjectMember projectMember) {
        projectMemberMapper.insert(projectMember);
    }

    public void removeProjectMember(Integer projectId, Integer userId) {
        projectMemberMapper.deleteByProjectAndUser(projectId, userId);
    }

    public static class ProjectStats {
        private int totalTasks;
        private int completedTasks;
        private int totalRisks;
        private int totalBugs;

        public ProjectStats(int totalTasks, int completedTasks, int totalRisks, int totalBugs) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.totalRisks = totalRisks;
            this.totalBugs = totalBugs;
        }

        public int getTotalTasks() { return totalTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public int getTotalRisks() { return totalRisks; }
        public int getTotalBugs() { return totalBugs; }

        public double getCompletionRate() {
            return totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
        }
    }
}
