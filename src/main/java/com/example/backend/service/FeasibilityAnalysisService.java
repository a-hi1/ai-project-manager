package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.FeasibilityAnalysis;
import com.example.backend.mapper.FeasibilityAnalysisMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeasibilityAnalysisService {
    @Autowired
    private FeasibilityAnalysisMapper feasibilityAnalysisMapper;
    
    public FeasibilityAnalysis selectById(Integer id) {
        return feasibilityAnalysisMapper.selectById(id);
    }
    
    public List<FeasibilityAnalysis> findByProjectId(Integer projectId) {
        LambdaQueryWrapper<FeasibilityAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeasibilityAnalysis::getProjectId, projectId);
        wrapper.orderByDesc(FeasibilityAnalysis::getCreatedAt);
        return feasibilityAnalysisMapper.selectList(wrapper);
    }
    
    public FeasibilityAnalysis createFeasibilityAnalysis(FeasibilityAnalysis feasibilityAnalysis) {
        feasibilityAnalysis.setCreatedAt(LocalDateTime.now());
        feasibilityAnalysis.setUpdatedAt(LocalDateTime.now());
        feasibilityAnalysisMapper.insert(feasibilityAnalysis);
        return feasibilityAnalysis;
    }
    
    public List<FeasibilityAnalysis> getByProjectId(Integer projectId) {
        return findByProjectId(projectId);
    }
    
    public void updateById(FeasibilityAnalysis feasibilityAnalysis) {
        feasibilityAnalysis.setUpdatedAt(LocalDateTime.now());
        feasibilityAnalysisMapper.updateById(feasibilityAnalysis);
    }
    
    public void removeById(Integer id) {
        feasibilityAnalysisMapper.deleteById(id);
    }
}
