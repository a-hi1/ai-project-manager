package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.RequirementResearch;
import com.example.backend.mapper.RequirementResearchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequirementResearchService {
    @Autowired
    private RequirementResearchMapper requirementResearchMapper;
    
    public RequirementResearch selectById(Integer id) {
        return requirementResearchMapper.selectById(id);
    }
    
    public List<RequirementResearch> findByProjectId(Integer projectId) {
        LambdaQueryWrapper<RequirementResearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequirementResearch::getProjectId, projectId);
        wrapper.orderByDesc(RequirementResearch::getCreatedAt);
        return requirementResearchMapper.selectList(wrapper);
    }
    
    public RequirementResearch createRequirementResearch(RequirementResearch requirementResearch) {
        requirementResearch.setCreatedAt(LocalDateTime.now());
        requirementResearch.setUpdatedAt(LocalDateTime.now());
        requirementResearchMapper.insert(requirementResearch);
        return requirementResearch;
    }
    
    public List<RequirementResearch> getByProjectId(Integer projectId) {
        return findByProjectId(projectId);
    }
    
    public void updateById(RequirementResearch requirementResearch) {
        requirementResearch.setUpdatedAt(LocalDateTime.now());
        requirementResearchMapper.updateById(requirementResearch);
    }
    
    public void removeById(Integer id) {
        requirementResearchMapper.deleteById(id);
    }
}
