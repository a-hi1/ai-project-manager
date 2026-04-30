package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private DeliverableMapper deliverableMapper;
    @Autowired
    private MilestoneMapper milestoneMapper;
    @Autowired
    private ChangeRequestMapper changeRequestMapper;
    @Autowired
    private UserMapper userMapper;

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

        // 计算项目周期
        Project project = projectMapper.selectById(projectId);
        Integer durationDays = 0;
        if (project != null && project.getStartDate() != null && project.getEndDate() != null) {
            durationDays = (int) ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate());
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
        retrospective.setDurationDays(durationDays);

        save(retrospective);
        return retrospective;
    }

    /**
     * 生成完整的项目结束报告
     */
    public Map<String, Object> generateProjectEndReport(Integer projectId) {
        System.out.println("开始生成项目结束报告，项目ID: " + projectId);
        Map<String, Object> report = new HashMap<>();

        try {
            // 1. 项目基本信息
            Project project = projectMapper.selectById(projectId);
            if (project == null) {
                throw new RuntimeException("项目不存在");
            }
            System.out.println("获取项目信息成功: " + project.getName());

        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("id", project.getId());
        projectInfo.put("name", project.getName());
        projectInfo.put("description", project.getDescription());
        projectInfo.put("status", project.getStatus());
        projectInfo.put("startDate", project.getStartDate());
        projectInfo.put("endDate", project.getEndDate());

        // 计算项目周期
        Integer durationDays = 0;
        if (project.getStartDate() != null && project.getEndDate() != null) {
            durationDays = (int) ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate());
        }
        projectInfo.put("durationDays", durationDays);

        // 获取项目经理信息
        User projectManager = userMapper.selectById(project.getCreatedBy());
        if (projectManager != null) {
            projectInfo.put("manager", projectManager.getUsername());
        }

        report.put("projectInfo", projectInfo);

        // 2. 任务统计
        List<Task> tasks = taskMapper.findByProjectId(projectId);
        Map<String, Object> taskStats = new HashMap<>();
        taskStats.put("total", tasks.size());
        taskStats.put("completed", tasks.stream().filter(t -> "done".equals(t.getStatus())).count());
        taskStats.put("inProgress", tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count());
        taskStats.put("pending", tasks.stream().filter(t -> "pending".equals(t.getStatus())).count());
        taskStats.put("overdue", tasks.stream().filter(t -> t.getEndDate() != null && t.getEndDate().isBefore(LocalDate.now()) && !"done".equals(t.getStatus())).count());

        BigDecimal completionRate = BigDecimal.ZERO;
        if (tasks.size() > 0) {
            completionRate = BigDecimal.valueOf((Long) taskStats.get("completed"))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(tasks.size()), 2, RoundingMode.HALF_UP);
        }
        taskStats.put("completionRate", completionRate);
        report.put("taskStats", taskStats);

        // 3. 工时统计
        BigDecimal totalHours = BigDecimal.ZERO;
        Map<Integer, BigDecimal> userHours = new HashMap<>();
        for (Task task : tasks) {
            List<WorkLog> workLogs = workLogMapper.findByTaskId(task.getId());
            for (WorkLog log : workLogs) {
                if (log.getHours() != null) {
                    totalHours = totalHours.add(log.getHours());
                    userHours.merge(log.getUserId(), log.getHours(), BigDecimal::add);
                }
            }
        }
        report.put("totalHours", totalHours);
        report.put("userHours", userHours);

        // 4. 风险统计
        List<Risk> risks = riskMapper.findByProjectId(projectId);
        Map<String, Object> riskStats = new HashMap<>();
        riskStats.put("total", risks.size());
        riskStats.put("resolved", risks.stream().filter(r -> "resolved".equals(r.getStatus())).count());
        riskStats.put("unresolved", risks.stream().filter(r -> !"resolved".equals(r.getStatus())).count());
        // 根据 probability * impact 计算风险等级
        riskStats.put("high", risks.stream().filter(r -> (r.getProbability() != null ? r.getProbability() : 0) * (r.getImpact() != null ? r.getImpact() : 0) >= 50).count());
        riskStats.put("medium", risks.stream().filter(r -> {
            int score = (r.getProbability() != null ? r.getProbability() : 0) * (r.getImpact() != null ? r.getImpact() : 0);
            return score >= 20 && score < 50;
        }).count());
        riskStats.put("low", risks.stream().filter(r -> (r.getProbability() != null ? r.getProbability() : 0) * (r.getImpact() != null ? r.getImpact() : 0) < 20).count());
        report.put("riskStats", riskStats);

        // 5. 缺陷统计
        List<Bug> bugs = bugMapper.findByProjectId(projectId);
        Map<String, Object> bugStats = new HashMap<>();
        bugStats.put("total", bugs.size());
        bugStats.put("resolved", bugs.stream().filter(b -> "resolved".equals(b.getStatus())).count());
        bugStats.put("unresolved", bugs.stream().filter(b -> !"resolved".equals(b.getStatus())).count());
        report.put("bugStats", bugStats);

        // 6. 里程碑统计
        List<Milestone> milestones = milestoneMapper.findByProjectId(projectId);
        Map<String, Object> milestoneStats = new HashMap<>();
        milestoneStats.put("total", milestones.size());
        milestoneStats.put("completed", milestones.stream().filter(m -> "completed".equals(m.getStatus())).count());
        milestoneStats.put("upcoming", milestones.stream().filter(m -> !"completed".equals(m.getStatus())).count());
        report.put("milestoneStats", milestoneStats);

        // 7. 变更请求统计
        List<ChangeRequest> changes = changeRequestMapper.findByProjectId(projectId);
        Map<String, Object> changeStats = new HashMap<>();
        changeStats.put("total", changes.size());
        changeStats.put("approved", changes.stream().filter(c -> "approved".equals(c.getStatus())).count());
        changeStats.put("pending", changes.stream().filter(c -> "pending".equals(c.getStatus())).count());
        changeStats.put("rejected", changes.stream().filter(c -> "rejected".equals(c.getStatus())).count());
        report.put("changeStats", changeStats);

        // 8. 交付物统计
        List<Deliverable> deliverables = deliverableMapper.findByProjectId(projectId);
        Map<String, Object> deliverableStats = new HashMap<>();
        deliverableStats.put("total", deliverables.size());
        deliverableStats.put("accepted", deliverables.stream().filter(d -> "accepted".equals(d.getStatus())).count());
        deliverableStats.put("pending", deliverables.stream().filter(d -> "pending".equals(d.getStatus())).count());
        deliverableStats.put("rejected", deliverables.stream().filter(d -> "rejected".equals(d.getStatus())).count());
        report.put("deliverableStats", deliverableStats);

        // 9. 获取复盘数据
        List<ProjectRetrospective> retrospectives = baseMapper.findByProjectId(projectId);
        if (!retrospectives.isEmpty()) {
            ProjectRetrospective retrospective = retrospectives.get(0);
            report.put("retrospective", retrospective);
        }

        // 10. 生成报告摘要
        StringBuilder summary = new StringBuilder();
        summary.append("项目《").append(project.getName()).append("》结束报告\n\n");
        summary.append("项目周期：").append(durationDays).append("天\n");
        summary.append("任务完成率：").append(completionRate).append("%\n");
        summary.append("总工时：").append(totalHours).append("小时\n");
        summary.append("风险解决率：").append(risks.size() > 0 ? BigDecimal.valueOf((Long) riskStats.get("resolved")).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(risks.size()), 2, RoundingMode.HALF_UP) : 0).append("%\n");
        summary.append("缺陷修复率：").append(bugs.size() > 0 ? BigDecimal.valueOf((Long) bugStats.get("resolved")).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(bugs.size()), 2, RoundingMode.HALF_UP) : 0).append("%\n");

            report.put("summary", summary.toString());
            report.put("generatedAt", LocalDateTime.now());

            System.out.println("项目结束报告生成成功");
            return report;
        } catch (Exception e) {
            System.err.println("生成报告时出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 导出项目结束报告为HTML格式
     */
    public String exportReportAsHtml(Integer projectId) {
        Map<String, Object> report = generateProjectEndReport(projectId);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>项目结束报告</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; background: #f5f7fa; }\n");
        html.append(".container { max-width: 900px; margin: 0 auto; background: white; padding: 40px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #303133; border-bottom: 2px solid #409eff; padding-bottom: 10px; }\n");
        html.append("h2 { color: #409eff; margin-top: 30px; }\n");
        html.append(".info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin: 20px 0; }\n");
        html.append(".info-item { background: #f5f7fa; padding: 15px; border-radius: 8px; }\n");
        html.append(".info-label { color: #909399; font-size: 12px; margin-bottom: 5px; }\n");
        html.append(".info-value { color: #303133; font-size: 16px; font-weight: bold; }\n");
        html.append(".stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin: 20px 0; }\n");
        html.append(".stat-card { background: #409eff; color: white; padding: 20px; border-radius: 8px; text-align: center; }\n");
        html.append(".stat-value { font-size: 28px; font-weight: bold; }\n");
        html.append(".stat-label { font-size: 12px; margin-top: 5px; opacity: 0.9; }\n");
        html.append(".section { margin: 30px 0; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }\n");
        html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ebeef5; }\n");
        html.append("th { background: #f5f7fa; font-weight: 600; color: #606266; }\n");
        html.append(".status-success { color: #67c23a; }\n");
        html.append(".status-warning { color: #e6a23c; }\n");
        html.append(".status-danger { color: #f56c6c; }\n");
        html.append("</style>\n</head>\n<body>\n");

        // 项目信息
        Map<String, Object> projectInfo = (Map<String, Object>) report.get("projectInfo");
        html.append("<div class=\"container\">\n");
        html.append("<h1>项目结束报告</h1>\n");

        html.append("<div class=\"section\">\n");
        html.append("<h2>项目基本信息</h2>\n");
        html.append("<div class=\"info-grid\">\n");
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">项目名称</div>\n");
        html.append("<div class=\"info-value\">").append(projectInfo.get("name")).append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">项目状态</div>\n");
        html.append("<div class=\"info-value\">").append(projectInfo.get("status")).append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">开始日期</div>\n");
        html.append("<div class=\"info-value\">").append(projectInfo.get("startDate")).append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">结束日期</div>\n");
        html.append("<div class=\"info-value\">").append(projectInfo.get("endDate")).append("</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">项目周期</div>\n");
        html.append("<div class=\"info-value\">").append(projectInfo.get("durationDays")).append(" 天</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">项目经理</div>\n");
        html.append("<div class=\"info-value\">").append(projectInfo.get("manager") != null ? projectInfo.get("manager") : "未指定").append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // 任务统计
        Map<String, Object> taskStats = (Map<String, Object>) report.get("taskStats");
        html.append("<div class=\"section\">\n");
        html.append("<h2>任务统计</h2>\n");
        html.append("<div class=\"stats-grid\">\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(taskStats.get("total")).append("</div>\n");
        html.append("<div class=\"stat-label\">总任务</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(taskStats.get("completed")).append("</div>\n");
        html.append("<div class=\"stat-label\">已完成</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(taskStats.get("completionRate")).append("%</div>\n");
        html.append("<div class=\"stat-label\">完成率</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(report.get("totalHours")).append("</div>\n");
        html.append("<div class=\"stat-label\">总工时</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // 风险统计
        Map<String, Object> riskStats = (Map<String, Object>) report.get("riskStats");
        html.append("<div class=\"section\">\n");
        html.append("<h2>风险统计</h2>\n");
        html.append("<div class=\"stats-grid\">\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(riskStats.get("total")).append("</div>\n");
        html.append("<div class=\"stat-label\">总风险</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(riskStats.get("resolved")).append("</div>\n");
        html.append("<div class=\"stat-label\">已解决</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(riskStats.get("high")).append("</div>\n");
        html.append("<div class=\"stat-label\">高风险</div>\n");
        html.append("</div>\n");
        html.append("<div class=\"stat-card\">\n");
        html.append("<div class=\"stat-value\">").append(riskStats.get("unresolved")).append("</div>\n");
        html.append("<div class=\"stat-label\">未解决</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // 报告生成时间
        html.append("<div style=\"margin-top: 40px; padding-top: 20px; border-top: 1px solid #ebeef5; color: #909399; font-size: 12px; text-align: center;\">\n");
        html.append("报告生成时间：").append(report.get("generatedAt")).append("\n");
        html.append("</div>\n");

        html.append("</div>\n");
        html.append("</body>\n</html>");

        return html.toString();
    }
}
