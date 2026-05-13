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
                projectInfo.put("managerId", projectManager.getId());
            }

            report.put("projectInfo", projectInfo);

            // 2. 任务统计（增强版）
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

            // 任务优先级分布
            Map<String, Long> priorityDistribution = new HashMap<>();
            priorityDistribution.put("high", tasks.stream().filter(t -> "high".equals(t.getPriority())).count());
            priorityDistribution.put("medium", tasks.stream().filter(t -> "medium".equals(t.getPriority())).count());
            priorityDistribution.put("low", tasks.stream().filter(t -> "low".equals(t.getPriority())).count());
            taskStats.put("priorityDistribution", priorityDistribution);

            report.put("taskStats", taskStats);
            report.put("tasks", tasks);

            // 3. 工时统计（增强版）
            BigDecimal totalHours = BigDecimal.ZERO;
            Map<Integer, BigDecimal> userHours = new HashMap<>();
            Map<String, Object> workLogDetail = new HashMap<>();
            
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

            // 4. 风险统计（增强版）
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
            report.put("risks", risks);

            // 5. 缺陷统计（增强版）
            List<Bug> bugs = bugMapper.findByProjectId(projectId);
            Map<String, Object> bugStats = new HashMap<>();
            bugStats.put("total", bugs.size());
            bugStats.put("resolved", bugs.stream().filter(b -> "resolved".equals(b.getStatus())).count());
            bugStats.put("unresolved", bugs.stream().filter(b -> !"resolved".equals(b.getStatus())).count());
            bugStats.put("critical", bugs.stream().filter(b -> "critical".equals(b.getSeverity())).count());
            bugStats.put("major", bugs.stream().filter(b -> "major".equals(b.getSeverity())).count());
            bugStats.put("minor", bugs.stream().filter(b -> "minor".equals(b.getSeverity())).count());
            report.put("bugStats", bugStats);
            report.put("bugs", bugs);

            // 6. 里程碑统计（增强版）
            List<Milestone> milestones = milestoneMapper.findByProjectId(projectId);
            Map<String, Object> milestoneStats = new HashMap<>();
            milestoneStats.put("total", milestones.size());
            milestoneStats.put("completed", milestones.stream().filter(m -> "completed".equals(m.getStatus())).count());
            milestoneStats.put("upcoming", milestones.stream().filter(m -> !"completed".equals(m.getStatus())).count());
            milestoneStats.put("delayed", milestones.stream().filter(m -> m.getDueDate() != null && m.getDueDate().isBefore(LocalDate.now()) && !"completed".equals(m.getStatus())).count());
            report.put("milestoneStats", milestoneStats);
            report.put("milestones", milestones);

            // 7. 变更请求统计（增强版）
            List<ChangeRequest> changes = changeRequestMapper.findByProjectId(projectId);
            Map<String, Object> changeStats = new HashMap<>();
            changeStats.put("total", changes.size());
            changeStats.put("approved", changes.stream().filter(c -> "approved".equals(c.getStatus())).count());
            changeStats.put("pending", changes.stream().filter(c -> "pending".equals(c.getStatus())).count());
            changeStats.put("rejected", changes.stream().filter(c -> "rejected".equals(c.getStatus())).count());
            
            // 变更影响统计（使用 impact 字段分析）
            Map<String, Long> changeImpact = new HashMap<>();
            // 由于没有 changeType 字段，简化为基于 status 的统计
            changeImpact.put("scope", 0L);
            changeImpact.put("schedule", 0L);
            changeImpact.put("quality", 0L);
            changeImpact.put("cost", 0L);
            changeStats.put("impactDistribution", changeImpact);
            
            report.put("changeStats", changeStats);
            report.put("changes", changes);

            // 8. 交付物统计（增强版）
            List<Deliverable> deliverables = deliverableMapper.findByProjectId(projectId);
            Map<String, Object> deliverableStats = new HashMap<>();
            deliverableStats.put("total", deliverables.size());
            deliverableStats.put("accepted", deliverables.stream().filter(d -> "accepted".equals(d.getStatus())).count());
            deliverableStats.put("pending", deliverables.stream().filter(d -> "pending".equals(d.getStatus())).count());
            deliverableStats.put("rejected", deliverables.stream().filter(d -> "rejected".equals(d.getStatus())).count());
            report.put("deliverableStats", deliverableStats);
            report.put("deliverables", deliverables);

            // 9. 团队成员统计
            List<User> allUsers = userMapper.selectList(null);
            Map<Integer, String> userMap = new HashMap<>();
            for (User user : allUsers) {
                userMap.put(user.getId(), user.getUsername());
            }
            report.put("userMap", userMap);

            // 10. 获取复盘数据
            List<ProjectRetrospective> retrospectives = baseMapper.findByProjectId(projectId);
            if (!retrospectives.isEmpty()) {
                ProjectRetrospective retrospective = retrospectives.get(0);
                report.put("retrospective", retrospective);
            }

            // 11. 生成详细报告摘要
            StringBuilder summary = new StringBuilder();
            summary.append("========================================\n");
            summary.append("          项目《").append(project.getName()).append("》结束报告\n");
            summary.append("========================================\n\n");

            // 11.1 项目概览
            summary.append("【一、项目概览】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 项目名称：").append(project.getName()).append("\n");
            if (project.getDescription() != null) {
                summary.append("• 项目描述：").append(project.getDescription()).append("\n");
            }
            summary.append("• 项目经理：").append(projectInfo.get("manager") != null ? projectInfo.get("manager") : "未指定").append("\n");
            summary.append("• 项目周期：").append(durationDays).append("天\n");
            if (project.getStartDate() != null) {
                summary.append("• 开始日期：").append(project.getStartDate()).append("\n");
            }
            if (project.getEndDate() != null) {
                summary.append("• 结束日期：").append(project.getEndDate()).append("\n");
            }
            summary.append("• 项目状态：").append(getStatusChineseName(project.getStatus())).append("\n\n");

            // 11.2 任务执行情况
            summary.append("【二、任务执行情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总任务数：").append(tasks.size()).append("\n");
            summary.append("• 已完成：").append(taskStats.get("completed")).append(" 个\n");
            summary.append("• 进行中：").append(taskStats.get("inProgress")).append(" 个\n");
            summary.append("• 待处理：").append(taskStats.get("pending")).append(" 个\n");
            summary.append("• 已延期：").append(taskStats.get("overdue")).append(" 个\n");
            summary.append("• 完成率：").append(completionRate).append("%\n");
            summary.append("• 优先级分布 - 高：").append(priorityDistribution.get("high"))
                    .append("，中：").append(priorityDistribution.get("medium"))
                    .append("，低：").append(priorityDistribution.get("low")).append("\n\n");

            // 11.3 工时投入情况
            summary.append("【三、工时投入情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总工时：").append(totalHours).append(" 小时\n");
            if (tasks.size() > 0) {
                BigDecimal avgHoursPerTask = totalHours.divide(BigDecimal.valueOf(tasks.size()), 2, RoundingMode.HALF_UP);
                summary.append("• 平均每任务工时：").append(avgHoursPerTask).append(" 小时\n");
            }
            if (!userHours.isEmpty()) {
                summary.append("• 成员工时分布：\n");
                for (Map.Entry<Integer, BigDecimal> entry : userHours.entrySet()) {
                    String username = userMap.get(entry.getKey());
                    if (username == null) username = "用户" + entry.getKey();
                    summary.append("  - ").append(username).append("：").append(entry.getValue()).append(" 小时\n");
                }
            }
            summary.append("\n");

            // 11.4 风险管理情况
            summary.append("【四、风险管理情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总风险数：").append(risks.size()).append("\n");
            summary.append("• 已解决：").append(riskStats.get("resolved")).append(" 个\n");
            summary.append("• 未解决：").append(riskStats.get("unresolved")).append(" 个\n");
            summary.append("• 风险等级 - 高：").append(riskStats.get("high"))
                    .append("，中：").append(riskStats.get("medium"))
                    .append("，低：").append(riskStats.get("low")).append("\n");
            if (risks.size() > 0) {
                BigDecimal riskResolutionRate = BigDecimal.valueOf((Long) riskStats.get("resolved"))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(risks.size()), 2, RoundingMode.HALF_UP);
                summary.append("• 风险解决率：").append(riskResolutionRate).append("%\n");
            }
            summary.append("\n");

            // 11.5 缺陷管理情况
            summary.append("【五、缺陷管理情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总缺陷数：").append(bugs.size()).append("\n");
            summary.append("• 已修复：").append(bugStats.get("resolved")).append(" 个\n");
            summary.append("• 未修复：").append(bugStats.get("unresolved")).append(" 个\n");
            summary.append("• 严重程度 - 致命：").append(bugStats.get("critical"))
                    .append("，重要：").append(bugStats.get("major"))
                    .append("，一般：").append(bugStats.get("minor")).append("\n");
            if (bugs.size() > 0) {
                BigDecimal bugFixRate = BigDecimal.valueOf((Long) bugStats.get("resolved"))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(bugs.size()), 2, RoundingMode.HALF_UP);
                summary.append("• 缺陷修复率：").append(bugFixRate).append("%\n");
            }
            summary.append("\n");

            // 11.6 里程碑完成情况
            summary.append("【六、里程碑完成情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总里程碑数：").append(milestones.size()).append("\n");
            summary.append("• 已完成：").append(milestoneStats.get("completed")).append(" 个\n");
            summary.append("• 待完成：").append(milestoneStats.get("upcoming")).append(" 个\n");
            summary.append("• 已延期：").append(milestoneStats.get("delayed")).append(" 个\n");
            if (milestones.size() > 0) {
                BigDecimal milestoneCompletionRate = BigDecimal.valueOf((Long) milestoneStats.get("completed"))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(milestones.size()), 2, RoundingMode.HALF_UP);
                summary.append("• 里程碑完成率：").append(milestoneCompletionRate).append("%\n");
            }
            summary.append("\n");

            // 11.7 变更管理情况
            summary.append("【七、变更管理情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总变更数：").append(changes.size()).append("\n");
            summary.append("• 已批准：").append(changeStats.get("approved")).append(" 个\n");
            summary.append("• 待审批：").append(changeStats.get("pending")).append(" 个\n");
            summary.append("• 已拒绝：").append(changeStats.get("rejected")).append(" 个\n\n");

            // 11.8 交付物验收情况
            summary.append("【八、交付物验收情况】\n");
            summary.append("----------------------------------------\n");
            summary.append("• 总交付物数：").append(deliverables.size()).append("\n");
            summary.append("• 已验收：").append(deliverableStats.get("accepted")).append(" 个\n");
            summary.append("• 待验收：").append(deliverableStats.get("pending")).append(" 个\n");
            summary.append("• 已拒绝：").append(deliverableStats.get("rejected")).append(" 个\n");
            if (deliverables.size() > 0) {
                BigDecimal deliverableAcceptanceRate = BigDecimal.valueOf((Long) deliverableStats.get("accepted"))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(deliverables.size()), 2, RoundingMode.HALF_UP);
                summary.append("• 交付物验收率：").append(deliverableAcceptanceRate).append("%\n");
            }
            summary.append("\n");

            // 11.9 总体评价
            summary.append("【九、项目总体评价】\n");
            summary.append("----------------------------------------\n");
            String overallEvaluation = generateOverallEvaluation(completionRate, durationDays, totalHours, risks.size(), bugs.size());
            summary.append(overallEvaluation).append("\n\n");

            // 11.10 经验教训
            summary.append("【十、经验教训】\n");
            summary.append("----------------------------------------\n");
            String lessonsLearned = generateLessonsLearned(completionRate, risks.size(), bugs.size(), changes.size());
            summary.append(lessonsLearned).append("\n\n");

            // 11.11 后续建议
            summary.append("【十一、后续建议】\n");
            summary.append("----------------------------------------\n");
            String recommendations = generateRecommendations(completionRate, risks.size(), bugs.size(), deliverables.size());
            summary.append(recommendations).append("\n\n");

            summary.append("========================================\n");
            summary.append("报告生成时间：").append(LocalDateTime.now()).append("\n");
            summary.append("========================================\n");

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

    private String getStatusChineseName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "pending": return "待启动";
            case "in_progress": return "进行中";
            case "completed": return "已完成";
            case "cancelled": return "已取消";
            default: return status;
        }
    }

    private String generateOverallEvaluation(BigDecimal completionRate, int durationDays, BigDecimal totalHours, int riskCount, int bugCount) {
        StringBuilder evaluation = new StringBuilder();
        
        // 根据完成率评价
        if (completionRate.compareTo(BigDecimal.valueOf(90)) >= 0) {
            evaluation.append("项目整体完成情况优秀，任务完成率达到 ").append(completionRate).append("%。\n");
        } else if (completionRate.compareTo(BigDecimal.valueOf(70)) >= 0) {
            evaluation.append("项目整体完成情况良好，任务完成率为 ").append(completionRate).append("%。\n");
        } else if (completionRate.compareTo(BigDecimal.valueOf(50)) >= 0) {
            evaluation.append("项目基本完成，任务完成率为 ").append(completionRate).append("%，尚有提升空间。\n");
        } else {
            evaluation.append("项目完成情况有待改进，任务完成率仅为 ").append(completionRate).append("%。\n");
        }

        // 工期评价
        if (durationDays > 0) {
            evaluation.append("项目历时 ").append(durationDays).append(" 天，");
            if (durationDays <= 30) {
                evaluation.append("属于短期项目，节奏紧凑。\n");
            } else if (durationDays <= 90) {
                evaluation.append("属于中期项目，进度控制合理。\n");
            } else {
                evaluation.append("属于长期项目，需要持续关注。\n");
            }
        }

        // 工时评价
        if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
            evaluation.append("项目总投入 ").append(totalHours).append(" 工时，");
            if (totalHours.compareTo(BigDecimal.valueOf(500)) > 0) {
                evaluation.append("投入规模较大，建议复盘工时分配合理性。\n");
            } else {
                evaluation.append("投入规模适中。\n");
            }
        }

        // 风险评价
        if (riskCount > 0) {
            evaluation.append("项目共识别 ").append(riskCount).append(" 项风险，");
            if (riskCount <= 3) {
                evaluation.append("风险数量较少，管理良好。\n");
            } else if (riskCount <= 10) {
                evaluation.append("风险数量适中，需持续关注。\n");
            } else {
                evaluation.append("风险数量较多，建议加强风险预判。\n");
            }
        }

        // 缺陷评价
        if (bugCount > 0) {
            evaluation.append("项目共发现 ").append(bugCount).append(" 个缺陷，");
            if (bugCount <= 5) {
                evaluation.append("质量控制较好。\n");
            } else if (bugCount <= 20) {
                evaluation.append("质量情况一般。\n");
            } else {
                evaluation.append("建议加强质量管理和测试环节。\n");
            }
        }

        return evaluation.toString();
    }

    private String generateLessonsLearned(BigDecimal completionRate, int riskCount, int bugCount, int changeCount) {
        StringBuilder lessons = new StringBuilder();

        lessons.append("1. 计划与执行方面：\n");
        if (completionRate.compareTo(BigDecimal.valueOf(80)) < 0) {
            lessons.append("   - 建议在项目初期加强需求分析和计划制定，提高任务预估的准确性。\n");
        } else {
            lessons.append("   - 项目计划执行较为到位，可将经验沉淀为组织过程资产。\n");
        }

        lessons.append("\n2. 风险管理方面：\n");
        if (riskCount > 5) {
            lessons.append("   - 建议在项目早期建立更完善的风险识别机制，提前制定应对预案。\n");
        } else {
            lessons.append("   - 风险识别与应对工作做得较好，可继续保持。\n");
        }

        lessons.append("\n3. 质量管理方面：\n");
        if (bugCount > 10) {
            lessons.append("   - 建议加强代码审查、单元测试等质量保障环节，减少缺陷流出。\n");
        } else {
            lessons.append("   - 质量控制工作值得肯定。\n");
        }

        lessons.append("\n4. 变更管理方面：\n");
        if (changeCount > 5) {
            lessons.append("   - 变更较为频繁，建议加强需求冻结和变更影响评估。\n");
        } else {
            lessons.append("   - 变更管理较为规范，范围控制良好。\n");
        }

        lessons.append("\n5. 团队协作方面：\n");
        lessons.append("   - 建议定期组织经验分享会，将个人经验转化为团队能力。\n");
        lessons.append("   - 重视文档记录，确保知识沉淀。\n");

        return lessons.toString();
    }

    private String generateRecommendations(BigDecimal completionRate, int riskCount, int bugCount, int deliverableCount) {
        StringBuilder recommendations = new StringBuilder();

        recommendations.append("1. 对后续项目的建议：\n");
        recommendations.append("   - 做好项目启动规划，明确目标、范围和验收标准。\n");
        recommendations.append("   - 建立定期进度评审机制，及时发现偏差并调整。\n");

        recommendations.append("\n2. 对组织层面的建议：\n");
        recommendations.append("   - 完善项目模板和 checklist，提高项目启动效率。\n");
        recommendations.append("   - 建立项目经验库，促进知识共享。\n");

        recommendations.append("\n3. 对技术层面的建议：\n");
        recommendations.append("   - 持续优化开发流程，提高开发效率和代码质量。\n");
        recommendations.append("   - 加强自动化测试建设，减少回归问题。\n");

        recommendations.append("\n4. 对当前项目后续工作：\n");
        if (completionRate.compareTo(BigDecimal.valueOf(100)) < 0) {
            recommendations.append("   - 建议妥善处理未完成任务，明确后续计划或关闭理由。\n");
        }
        if (deliverableCount > 0) {
            recommendations.append("   - 做好项目文档和交付物的归档，便于后续查阅。\n");
        }
        recommendations.append("   - 组织项目复盘会议，深入讨论经验教训。\n");

        return recommendations.toString();
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
