package com.example.backend.service;

import com.example.backend.entity.Project;
import com.example.backend.entity.ProjectMember;
import com.example.backend.mapper.ProjectMapper;
import com.example.backend.mapper.ProjectMemberMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1);
        testProject.setName("测试项目");
        testProject.setDescription("项目描述");
        testProject.setStatus("pending");
        testProject.setStartDate(LocalDate.now());
        testProject.setEndDate(LocalDate.now().plusMonths(3));
    }

    @Test
    void testGetById() {
        when(projectMapper.selectById(1)).thenReturn(testProject);

        Project found = projectService.getById(1);

        assertNotNull(found);
        assertEquals("测试项目", found.getName());
        assertEquals("pending", found.getStatus());
    }

    @Test
    void testFindAll() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectMapper.findAll()).thenReturn(projects);

        List<Project> result = projectService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试项目", result.get(0).getName());
    }

    @Test
    void testGetProjectMembers() {
        ProjectMember member = new ProjectMember();
        member.setProjectId(1);
        member.setUserId(1);
        member.setRole("开发");

        when(projectMemberMapper.findByProjectId(1)).thenReturn(Arrays.asList(member));

        List<ProjectMember> members = projectService.getProjectMembers(1);

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals("开发", members.get(0).getRole());
    }

    @Test
    void testGetProjectStats() {
        when(projectMapper.countTasksByProjectId(1)).thenReturn(10);
        when(projectMapper.countCompletedTasksByProjectId(1)).thenReturn(5);
        when(projectMapper.countRisksByProjectId(1)).thenReturn(3);
        when(projectMapper.countBugsByProjectId(1)).thenReturn(2);

        ProjectService.ProjectStats stats = projectService.getProjectStats(1);

        assertNotNull(stats);
        assertEquals(10, stats.getTotalTasks());
        assertEquals(5, stats.getCompletedTasks());
        assertEquals(3, stats.getTotalRisks());
        assertEquals(2, stats.getTotalBugs());
        assertEquals(50.0, stats.getCompletionRate());
    }
}
