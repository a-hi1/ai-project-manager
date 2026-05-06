package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {
    private final ChatLanguageModel chatModel;
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;
    private final TaskService taskService;
    private final RiskService riskService;
    private final BugService bugService;
    private final WorkLogService workLogService;
    private final ChangeRequestService changeRequestService;
    private final DeliverableService deliverableService;
    private final ProjectRetrospectiveService retrospectiveService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    public AiService(ChatLanguageModel chatModel, TaskMapper taskMapper, 
                   ProjectMapper projectMapper, TaskService taskService,
                   RiskService riskService, BugService bugService,
                   WorkLogService workLogService, ChangeRequestService changeRequestService,
                   DeliverableService deliverableService, ProjectRetrospectiveService retrospectiveService,
                   KnowledgeDocumentService knowledgeDocumentService) {
        this.chatModel = chatModel;
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
        this.taskService = taskService;
        this.riskService = riskService;
        this.bugService = bugService;
        this.workLogService = workLogService;
        this.changeRequestService = changeRequestService;
        this.deliverableService = deliverableService;
        this.retrospectiveService = retrospectiveService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    public String chat(String message) {
        return chatModel.generate(message);
    }

    public String chatStream(String message) {
        return chatModel.generate(message);
    }

    public String parseRequirementDocument(String content) {
        String prompt = """
                你是一个专业的需求分析专家。请分析以下需求文档，提取功能点、业务规则，并检测潜在的需求冲突，最后给出优化建议。

                需求文档内容：
                %s

                请严格以JSON格式返回结果，不要包含任何其他文字说明。JSON格式如下：
                {
                  "功能点": ["功能1描述", "功能2描述"],
                  "业务规则": ["规则1描述", "规则2描述"],
                  "需求冲突": ["冲突1描述", "冲突2描述"],
                  "优化建议": ["建议1描述", "建议2描述"]
                }
                
                确保返回的是纯JSON字符串，不包含任何markdown格式标记。
                """.formatted(content);

        try {
            String result = chatModel.generate(prompt);
            System.out.println("AI返回的原始结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("调用AI服务失败: " + e.getMessage());
            e.printStackTrace();
            // 返回一个友好的错误信息
            return """
                    {
                      "功能点": ["AI服务暂时不可用，请稍后再试"],
                      "业务规则": ["请检查API配置或网络连接"],
                      "需求冲突": [],
                      "优化建议": ["确保智谱AI API Key配置正确", "检查网络连接是否正常"]
                    }
                    """;
        }
    }

    public String splitTask(String requirement) {
        String prompt = """
                你是一个资深的项目经理。请根据以下需求，自动拆分成WBS任务结构，识别任务间的依赖关系。

                需求内容：
                %s

                请严格以JSON格式返回结果，不要包含任何其他文字说明。JSON格式如下：
                {
                  "任务列表": [
                    {
                      "任务名称": "任务1名称",
                      "描述": "任务1详细描述",
                      "工期": 3,
                      "前置任务": []
                    },
                    {
                      "任务名称": "任务2名称",
                      "描述": "任务2详细描述",
                      "工期": 2,
                      "前置任务": ["任务1名称"]
                    }
                  ]
                }
                
                确保返回的是纯JSON字符串，不包含任何markdown格式标记。
                """.formatted(requirement);

        try {
            String result = chatModel.generate(prompt);
            System.out.println("AI拆分任务返回的原始结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("调用AI拆分任务服务失败: " + e.getMessage());
            e.printStackTrace();
            // 返回一个友好的错误信息
            return """
                    {
                      "任务列表": [
                        {
                          "任务名称": "AI服务暂时不可用",
                          "描述": "请检查API配置或网络连接",
                          "工期": 1,
                          "前置任务": []
                        }
                      ]
                    }
                    """;
        }
    }

    public String analyzeProjectStatus(Integer projectId) {
        String prompt = """
            请分析以下项目的状态数据，包括任务进度、风险情况、工时统计等。

            项目ID：%d

            请给出：
            1. 项目整体健康状况评估
            2. 当前面临的主要风险
            3. 建议的改进措施
            """.formatted(projectId);

        return chatModel.generate(prompt);
    }

    public String answerQuestion(String question, String context) {
        String prompt = """
            基于以下上下文信息，回答用户的问题。如果上下文中没有相关信息，请说明无法回答。

            上下文信息：
            %s

            用户问题：
            %s
            """.formatted(context, question);

        return chatModel.generate(prompt);
    }

    public ProgressPrediction predictProgress(Integer projectId) {
        List<Task> tasks = taskMapper.findByProjectId(projectId);
        if (tasks.isEmpty()) {
            return new ProgressPrediction(0, 0, 0.0, LocalDate.now(), LocalDate.now(), "无任务数据", 0.0);
        }

        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
        int inProgressTasks = (int) tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();

        long totalDuration = tasks.stream()
            .filter(t -> t.getDuration() != null)
            .mapToLong(Task::getDuration)
            .sum();

        long completedDuration = tasks.stream()
            .filter(t -> "done".equals(t.getStatus()) && t.getDuration() != null)
            .mapToLong(Task::getDuration)
            .sum();

        double avgProgress = totalDuration > 0 ? (double) completedDuration / totalDuration * 100 : 0;

        LocalDate now = LocalDate.now();
        long avgDaysPerTask = totalTasks > 0 ? totalDuration / totalTasks : 1;
        long remainingDays = (long) ((totalDuration - completedDuration) / (completedTasks > 0 ? (double) completedDuration / completedTasks : 1) * avgDaysPerTask);
        LocalDate predictedEndDate = now.plusDays(Math.max(0, remainingDays));

        Task latestTask = tasks.stream()
            .filter(t -> t.getEndDate() != null && !"done".equals(t.getStatus()))
            .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
            .findFirst()
            .orElse(null);

        LocalDate originalEndDate = latestTask != null ? latestTask.getEndDate() : now.plusDays(totalDuration);

        long daysUntilDeadline = ChronoUnit.DAYS.between(now, originalEndDate);
        long daysUntilPredicted = ChronoUnit.DAYS.between(now, predictedEndDate);

        double onTimeProbability;
        String riskAssessment;
        if (daysUntilPredicted <= daysUntilDeadline) {
            onTimeProbability = 0.85 + (daysUntilDeadline - daysUntilPredicted) * 0.01;
            riskAssessment = "项目进度正常，预计能按时完成";
        } else {
            onTimeProbability = Math.max(0.1, 0.5 - (daysUntilPredicted - daysUntilDeadline) * 0.05);
            riskAssessment = "项目存在延期风险，预计延期 " + (daysUntilPredicted - daysUntilDeadline) + " 天";
        }
        onTimeProbability = Math.min(0.99, onTimeProbability);

        return new ProgressPrediction(
            totalTasks,
            completedTasks,
            avgProgress,
            originalEndDate,
            predictedEndDate,
            riskAssessment,
            onTimeProbability
        );
    }

    public SmartFormFill suggestProjectPlan(String projectName, String coreGoal) {
        String prompt = """
            你是一个资深项目经理。根据以下项目信息，推荐合适的项目计划。

            项目名称：%s
            核心目标：%s

            请以JSON格式返回，格式如下：
            {
                "建议周期": {
                    "开始日期": "建议的开始日期（YYYY-MM-DD格式，距今7天后）",
                    "结束日期": "建议的结束日期（YYYY-MM-DD格式）",
                    "总工期": 月数
                },
                "团队配置": {
                    "项目经理": 1,
                    "开发人员": 数量,
                    "测试人员": 数量,
                    "其他角色": ["角色1:数量", ...]
                },
                "初始任务": [
                    {
                        "任务名称": "任务1",
                        "工期": 天数,
                        "描述": "任务描述"
                    }
                ]
            }
            项目周期建议：
            - 小型项目（核心功能单一）：2-3个月
            - 中型项目（多功能系统）：3-6个月
            - 大型项目（复杂企业系统）：6-12个月

            团队配置建议：
            - 每3个开发人员配1个测试人员
            - 项目经理1人
            - 根据工期调整人数

            请直接返回JSON，不要有其他文字。
            """.formatted(projectName, coreGoal);

        String aiResponse = chatModel.generate(prompt);
        return new SmartFormFill(aiResponse);
    }

    public record ProgressPrediction(
        int totalTasks,
        int completedTasks,
        double overallProgress,
        LocalDate originalEndDate,
        LocalDate predictedEndDate,
        String riskAssessment,
        double onTimeProbability
    ) {}

    public record SmartFormFill(String aiSuggestion) {}

    public String getProjectContext(Integer projectId) {
        StringBuilder context = new StringBuilder();
        
        Project project = projectMapper.selectById(projectId);
        if (project != null) {
            context.append("【项目信息】\n");
            context.append("项目名称: ").append(project.getName()).append("\n");
            context.append("项目描述: ").append(project.getDescription()).append("\n");
            context.append("项目状态: ").append(project.getStatus()).append("\n");
            context.append("开始日期: ").append(project.getStartDate()).append("\n");
            context.append("结束日期: ").append(project.getEndDate()).append("\n\n");
        }

        List<Task> tasks = taskService.getTasksByProjectIdList(projectId);
        if (!tasks.isEmpty()) {
            context.append("【任务信息】\n");
            long totalTasks = tasks.size();
            long doneTasks = tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
            long inProgressTasks = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
            long todoTasks = tasks.stream().filter(t -> "todo".equals(t.getStatus())).count();
            long reviewTasks = tasks.stream().filter(t -> "review".equals(t.getStatus())).count();
            
            context.append("总任务数: ").append(totalTasks).append("\n");
            context.append("已完成: ").append(doneTasks).append("\n");
            context.append("进行中: ").append(inProgressTasks).append("\n");
            context.append("待审核: ").append(reviewTasks).append("\n");
            context.append("待处理: ").append(todoTasks).append("\n");
            context.append("完成率: ").append(totalTasks > 0 ? String.format("%.1f", (double) doneTasks / totalTasks * 100) : 0).append("%\n");
            
            context.append("\n近期任务:\n");
            tasks.stream()
                .limit(10)
                .forEach(t -> {
                    context.append("- ").append(t.getName());
                    if (t.getPriority() != null) {
                        context.append(" (优先级: ").append(t.getPriority()).append(")");
                    }
                    context.append(" [").append(t.getStatus()).append("]\n");
                });
            context.append("\n");
        }

        List<Risk> risks = riskService.getRisksByProjectId(projectId);
        if (!risks.isEmpty()) {
            context.append("【风险信息】\n");
            context.append("总风险数: ").append(risks.size()).append("\n");
            long openRisks = risks.stream().filter(r -> "open".equals(r.getStatus())).count();
            long monitoringRisks = risks.stream().filter(r -> "monitoring".equals(r.getStatus())).count();
            long resolvedRisks = risks.stream().filter(r -> "resolved".equals(r.getStatus())).count();
            
            context.append("开放风险: ").append(openRisks).append("\n");
            context.append("监控中: ").append(monitoringRisks).append("\n");
            context.append("已解决: ").append(resolvedRisks).append("\n");
            
            if (openRisks > 0) {
                context.append("主要风险:\n");
                risks.stream()
                    .filter(r -> "open".equals(r.getStatus()))
                    .limit(5)
                    .forEach(r -> {
                        context.append("- ").append(r.getName());
                        if (r.getProbability() != null && r.getImpact() != null) {
                            context.append(" (概率: ").append(r.getProbability()).append("/10, 影响: ").append(r.getImpact()).append("/10)");
                        }
                        context.append("\n");
                    });
            }
            context.append("\n");
        }

        List<Bug> bugs = bugService.getByProjectId(projectId);
        if (!bugs.isEmpty()) {
            context.append("【缺陷信息】\n");
            context.append("总缺陷数: ").append(bugs.size()).append("\n");
            long openBugs = bugs.stream().filter(b -> "open".equals(b.getStatus())).count();
            long assignedBugs = bugs.stream().filter(b -> "assigned".equals(b.getStatus())).count();
            long resolvedBugs = bugs.stream().filter(b -> "resolved".equals(b.getStatus())).count();
            
            context.append("开放缺陷: ").append(openBugs).append("\n");
            context.append("已分配: ").append(assignedBugs).append("\n");
            context.append("已解决: ").append(resolvedBugs).append("\n");
            
            if (openBugs > 0) {
                context.append("主要缺陷:\n");
                bugs.stream()
                    .filter(b -> "open".equals(b.getStatus()))
                    .limit(5)
                    .forEach(b -> {
                        context.append("- ").append(b.getTitle());
                        if (b.getSeverity() != null) {
                            context.append(" (严重程度: ").append(b.getSeverity()).append(")");
                        }
                        context.append("\n");
                    });
            }
            context.append("\n");
        }

        context.append("【AI指令】\n");
        context.append("你是一位经验丰富的项目经理助理，请根据以上项目信息帮助用户回答问题、分析情况、提供建议。\n");
        context.append("如果用户询问项目相关问题，请结合上述数据给出专业、实用的回答。\n");
        
        return context.toString();
    }

    public String chatWithContext(String message, Integer projectId) {
        System.out.println("===== AiService.chatWithContext 开始 =====");
        System.out.println("输入消息: " + message);
        System.out.println("项目ID: " + projectId);
        
        String systemPrompt = "你是一位专业的AI项目管理助手，专注于帮助项目经理更好地管理项目。" +
                            "你能够提供项目分析、任务建议、风险评估、进度跟踪等方面的帮助。" +
                            "回答应该专业、实用、具体。" +
                            "【重要】回答格式要求：不要使用Markdown格式，不要使用###、##、#等标题符号，" +
                            "不要使用**加粗**、*斜体*等格式，直接用自然的中文分段回答，" +
                            "每段开头空两格，段落之间空一行。";
        
        StringBuilder fullContext = new StringBuilder(systemPrompt + "\n\n");
        
        if (projectId != null) {
            System.out.println("获取项目上下文...");
            String projectContext = getProjectContext(projectId);
            fullContext.append(projectContext).append("\n\n");
            System.out.println("项目上下文长度: " + projectContext.length());
        }
        
        System.out.println("搜索知识库相关内容...");
        List<KnowledgeDocument> relevantDocs = knowledgeDocumentService.searchBySemantic(projectId, message, 3);
        System.out.println("找到相关文档数量: " + relevantDocs.size());
        
        if (!relevantDocs.isEmpty()) {
            fullContext.append("【知识库相关内容】\n");
            for (int i = 0; i < relevantDocs.size(); i++) {
                KnowledgeDocument doc = relevantDocs.get(i);
                fullContext.append("相关文档 ").append(i + 1).append(": ").append(doc.getTitle()).append("\n");
                fullContext.append(doc.getContent()).append("\n\n");
            }
        }
        
        fullContext.append("用户问题: ").append(message);
        
        System.out.println("完整上下文长度: " + fullContext.length());
        System.out.println("开始调用chatModel.generate...");
        
        try {
            String result = chatModel.generate(fullContext.toString());
            System.out.println("chatModel.generate调用成功，结果长度: " + result.length());
            System.out.println("===== AiService.chatWithContext 结束 =====");
            return result;
        } catch (Exception e) {
            System.out.println("===== chatModel.generate调用失败 =====");
            System.out.println("错误类型: " + e.getClass().getName());
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String analyzeProjectHealth(Integer projectId) {
        String context = getProjectContext(projectId);
        String prompt = """
            请基于以下项目信息，进行全面的项目健康度分析：
            
            %s
            
            请提供以下内容：
            1. 项目整体健康度评分（0-100分）
            2. 优势分析（至少3点）
            3. 存在问题（至少3点）
            4. 改进建议（具体、可操作）
            5. 下一步行动建议
            """.formatted(context);
        
        return chatModel.generate(prompt);
    }

    public String generateDailyReport(Integer projectId) {
        String context = getProjectContext(projectId);
        String prompt = """
            请基于以下项目信息，生成一份简洁的项目日报：
            
            %s
            
            日报内容应包括：
            1. 今日进展（任务完成情况）
            2. 当前问题（风险和缺陷）
            3. 明日计划
            4. 需要协调的事项
            """.formatted(context);
        
        return chatModel.generate(prompt);
    }

    public String suggestTasks(Integer projectId) {
        String context = getProjectContext(projectId);
        String prompt = """
            请基于以下项目信息，分析当前项目状态并建议下一步优先处理的任务：
            
            %s
            
            请提供：
            1. 3-5个优先级最高的建议任务
            2. 每个任务的预期成果和时间安排
            3. 建议的负责人角色
            """.formatted(context);
        
        return chatModel.generate(prompt);
    }

    public String summarizeProject(Integer projectId) {
        String context = getProjectContext(projectId);
        String prompt = """
            请基于以下项目信息，生成一份项目总结报告：
            
            %s
            
            总结内容应包括：
            1. 项目概况（一句话介绍）
            2. 当前进度概述
            3. 项目亮点
            4. 主要挑战
            5. 关键建议
            """.formatted(context);
        
        return chatModel.generate(prompt);
    }
}
