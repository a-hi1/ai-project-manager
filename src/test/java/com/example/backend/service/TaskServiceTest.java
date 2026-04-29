package com.example.backend.service;

import com.example.backend.entity.Task;
import com.example.backend.mapper.TaskDependencyMapper;
import com.example.backend.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskDependencyMapper taskDependencyMapper;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1);
        testTask.setProjectId(1);
        testTask.setName("测试任务");
        testTask.setDescription("任务描述");
        testTask.setStatus("todo");
        testTask.setPriority("high");
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(7));
        testTask.setDuration(7);
        testTask.setProgress(0);
        testTask.setLevel(0);
    }

    @Test
    void testGetTasksByProjectIdList() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskMapper.findByProjectId(1)).thenReturn(tasks);

        List<Task> result = taskService.getTasksByProjectIdList(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试任务", result.get(0).getName());
    }

    @Test
    void testGetChildTasks() {
        List<Task> childTasks = Arrays.asList(testTask);
        when(taskMapper.findByParentId(1)).thenReturn(childTasks);

        List<Task> result = taskService.getChildTasks(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMilestonesByProjectId() {
        testTask.setIsMilestone(true);
        List<Task> milestones = Arrays.asList(testTask);
        when(taskMapper.findMilestonesByProjectId(1)).thenReturn(milestones);

        List<Task> result = taskService.getMilestonesByProjectId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetTaskStatsByProjectId() {
        Task completedTask = new Task();
        completedTask.setStatus("done");
        completedTask.setDuration(5);
        completedTask.setProjectId(1);

        Task inProgressTask = new Task();
        inProgressTask.setStatus("in_progress");
        inProgressTask.setDuration(3);
        inProgressTask.setProjectId(1);

        when(taskMapper.findByProjectId(1)).thenReturn(Arrays.asList(testTask, completedTask, inProgressTask));

        TaskService.TaskStats stats = taskService.getTaskStatsByProjectId(1);

        assertNotNull(stats);
        assertEquals(3, stats.getTotal());
        assertEquals(1, stats.getCompleted());
        assertEquals(1, stats.getInProgress());
        assertEquals(1, stats.getTodo());
    }

    @Test
    void testAddDependency() {
        var dependency = new com.example.backend.entity.TaskDependency();
        dependency.setTaskId(1);
        dependency.setDependsOnId(2);

        when(taskDependencyMapper.insert(any())).thenReturn(1);

        var result = taskService.addDependency(dependency);

        assertNotNull(result);
        verify(taskDependencyMapper).insert(dependency);
    }
}
