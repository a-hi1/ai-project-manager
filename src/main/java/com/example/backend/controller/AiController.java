package com.example.backend.controller;

import com.example.backend.entity.AiConversation;
import com.example.backend.entity.KnowledgeDocument;
import com.example.backend.entity.Task;
import com.example.backend.entity.Risk;
import com.example.backend.entity.Bug;
import com.example.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private AiConversationService aiConversationService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private BugService bugService;

    @Autowired
    private WorkLogService workLogService;

    @PostMapping("/upload-document")
    public Map<String, Object> uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam("projectId") Integer projectId) {
        try {
            String fileName = file.getOriginalFilename();
            String content = new String(file.getBytes());

            String result = aiService.parseRequirementDocument(content);
            return Map.of("success", true, "data", result, "fileName", fileName);
        } catch (Exception e) {
            return Map.of("success", false, "message", "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/parse-document")
    public Map<String, Object> parseDocument(@RequestBody Map<String, Object> data) {
        try {
            String content = (String) data.get("content");
            String result = aiService.parseRequirementDocument(content);
            return Map.of("success", true, "data", result);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/split-task")
    public Map<String, Object> splitTask(@RequestBody Map<String, Object> data) {
        try {
            String requirement = (String) data.get("requirement");
            String result = aiService.splitTask(requirement);
            return Map.of("success", true, "data", result);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> data) {
        try {
            String userMessage = (String) data.get("message");
            String aiResponse;
            
            try {
                aiResponse = aiService.chat(userMessage);
            } catch (Exception e) {
                aiResponse = "抱歉，AI服务暂时不可用，请稍后再试。";
            }

            return Map.of("success", true, "data", aiResponse);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/conversation/{projectId}")
    public Map<String, Object> getConversation(@PathVariable Integer projectId) {
        try {
            List<AiConversation> conversations = aiConversationService.getByProjectId(projectId);
            return Map.of("success", true, "data", conversations);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/knowledge/{projectId}")
    public Map<String, Object> getKnowledge(@PathVariable Integer projectId) {
        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.getByProjectId(projectId);
            return Map.of("success", true, "data", documents);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/knowledge/add")
    public Map<String, Object> addKnowledge(@RequestBody KnowledgeDocument document) {
        try {
            knowledgeDocumentService.create(document);
            return Map.of("success", true, "message", "知识添加成功");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/knowledge/search/{projectId}")
    public Map<String, Object> searchKnowledge(@PathVariable Integer projectId, @RequestParam String keyword) {
        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.searchByKeyword(projectId, keyword);
            return Map.of("success", true, "data", documents);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/generate-retrospective-summary")
    public Map<String, Object> generateRetrospectiveSummary(@RequestBody Map<String, Object> data) {
        try {
            Integer projectId = (Integer) data.get("projectId");

            List<Task> tasks = taskService.getTasksByProjectIdList(projectId);
            List<Risk> risks = riskService.getRisksByProjectId(projectId);
            List<Bug> bugs = bugService.getByProjectId(projectId);

            int totalTasks = tasks.size();
            int completedTasks = (int) tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
            int totalRisks = risks.size();
            int resolvedRisks = (int) risks.stream().filter(r -> "resolved".equals(r.getStatus())).count();
            int totalBugs = bugs.size();
            int resolvedBugs = (int) bugs.stream().filter(b -> "resolved".equals(b.getStatus())).count();

            String prompt = """
                请基于以下项目数据，生成一份项目复盘总结：

                任务统计：
                - 总任务数：%d
                - 已完成任务：%d
                - 任务完成率：%.2f%%

                风险统计：
                - 总风险数：%d
                - 已解决风险：%d

                缺陷统计：
                - 总缺陷数：%d
                - 已修复缺陷：%d

                请生成一份简洁的项目复盘总结，包括：主要成就、遇到的问题、经验教训。
                """.formatted(
                    totalTasks, completedTasks,
                    totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0,
                    totalRisks, resolvedRisks,
                    totalBugs, resolvedBugs
            );

            String summary = aiService.chat(prompt);
            return Map.of("success", true, "data", summary);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/predict-progress/{projectId}")
    public Map<String, Object> predictProgress(@PathVariable Integer projectId) {
        try {
            AiService.ProgressPrediction prediction = aiService.predictProgress(projectId);
            return Map.of("success", true, "data", prediction);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/suggest-plan")
    public Map<String, Object> suggestProjectPlan(@RequestBody Map<String, Object> data) {
        try {
            String projectName = (String) data.get("projectName");
            String coreGoal = (String) data.get("coreGoal");
            AiService.SmartFormFill suggestion = aiService.suggestProjectPlan(projectName, coreGoal);
            return Map.of("success", true, "data", suggestion);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}