package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.ProjectRetrospective;
import com.example.backend.entity.Risk;
import com.example.backend.entity.Bug;
import com.example.backend.entity.Task;
import com.example.backend.entity.WorkLog;
import com.example.backend.mapper.ProjectRetrospectiveMapper;
import com.example.backend.mapper.RiskMapper;
import com.example.backend.mapper.BugMapper;
import com.example.backend.mapper.TaskMapper;
import com.example.backend.mapper.WorkLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ProjectRetrospectiveService extends ServiceImpl<ProjectRetrospectiveMapper, ProjectRetrospective> {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private RiskMapper riskMapper;
    @Autowired
    private BugMapper bugMapper;
    @Autowired
    private WorkLogMapper workLogMapper;

    public List<ProjectRetrospective> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public ProjectRetrospective generateRetrospective(Integer projectId, Integer createdBy) {
        List<Task> tasks = taskMapper.findByProjectId(projectId);
        List<Risk> risks = riskMapper.findByProjectId(projectId);
        List<Bug> bugs = bugMapper.findByProjectId(projectId);

        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
        int totalRisks = risks.size();
        int resolvedRisks = (int) risks.stream().filter(r -> "resolved".equals(r.getStatus())).count();
        int totalBugs = bugs.size();
        int resolvedBugs = (int) bugs.stream().filter(b -> "resolved".equals(b.getStatus())).count();

        BigDecimal taskCompletionRate = BigDecimal.ZERO;
        if (totalTasks > 0) {
            taskCompletionRate = BigDecimal.valueOf(completedTasks)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTasks), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalHours = BigDecimal.ZERO;
        for (Task task : tasks) {
            List<WorkLog> workLogs = workLogMapper.findByTaskId(task.getId());
            for (WorkLog log : workLogs) {
                if (log.getHours() != null) {
                    totalHours = totalHours.add(log.getHours());
                }
            }
        }

        ProjectRetrospective retrospective = new ProjectRetrospective();
        retrospective.setProjectId(projectId);
        retrospective.setCreatedBy(createdBy);
        retrospective.setTotalTasks(totalTasks);
        retrospective.setCompletedTasks(completedTasks);
        retrospective.setTaskCompletionRate(taskCompletionRate);
        retrospective.setTotalRisks(totalRisks);
        retrospective.setResolvedRisks(resolvedRisks);
        retrospective.setTotalBugs(totalBugs);
        retrospective.setResolvedBugs(resolvedBugs);
        retrospective.setTotalHours(totalHours);

        save(retrospective);
        return retrospective;
    }
}