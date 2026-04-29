package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Milestone;
import com.example.backend.mapper.MilestoneMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilestoneService extends ServiceImpl<MilestoneMapper, Milestone> {
    public List<Milestone> getMilestonesByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public Milestone createMilestone(Milestone milestone) {
        save(milestone);
        return milestone;
    }
}