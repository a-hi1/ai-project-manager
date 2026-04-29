package com.example.backend.service;

import com.example.backend.entity.Task;
import com.example.backend.mapper.TaskMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatLanguageModel chatModel;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testChat() {
        when(chatModel.generate("Hello")).thenReturn("Hi, how can I help you?");

        String response = aiService.chat("Hello");

        assertNotNull(response);
        assertEquals("Hi, how can I help you?", response);
        verify(chatModel).generate("Hello");
    }

    @Test
    void testParseRequirementDocument() {
        String content = "需求：用户登录功能";
        String expectedResponse = "{\"功能点\": [\"登录\"]}";
        when(chatModel.generate(anyString())).thenReturn(expectedResponse);

        String result = aiService.parseRequirementDocument(content);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void testSplitTask() {
        String requirement = "开发一个电商系统";
        String expectedResponse = "{\"任务列表\": []}";
        when(chatModel.generate(anyString())).thenReturn(expectedResponse);

        String result = aiService.splitTask(requirement);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void testPredictProgress_NoTasks() {
        when(taskMapper.findByProjectId(1)).thenReturn(Collections.emptyList());

        AiService.ProgressPrediction prediction = aiService.predictProgress(1);

        assertNotNull(prediction);
        assertEquals(0, prediction.totalTasks());
        assertEquals("无任务数据", prediction.riskAssessment());
    }

    @Test
    void testPredictProgress_WithTasks() {
        Task task1 = new Task();
        task1.setStatus("done");
        task1.setDuration(5);
        task1.setEndDate(LocalDate.now().plusDays(5));

        Task task2 = new Task();
        task2.setStatus("in_progress");
        task2.setDuration(3);
        task2.setEndDate(LocalDate.now().plusDays(10));

        when(taskMapper.findByProjectId(1)).thenReturn(Arrays.asList(task1, task2));

        AiService.ProgressPrediction prediction = aiService.predictProgress(1);

        assertNotNull(prediction);
        assertEquals(2, prediction.totalTasks());
        assertEquals(1, prediction.completedTasks());
        assertTrue(prediction.overallProgress() > 0);
    }

    @Test
    void testSuggestProjectPlan() {
        String expectedResponse = "{\"建议周期\": {}}";
        when(chatModel.generate(anyString())).thenReturn(expectedResponse);

        AiService.SmartFormFill result = aiService.suggestProjectPlan("电商系统", "在线购物平台");

        assertNotNull(result);
        assertEquals(expectedResponse, result.aiSuggestion());
    }
}
