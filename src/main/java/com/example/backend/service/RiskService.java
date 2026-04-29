package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Risk;
import com.example.backend.mapper.RiskMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskService extends ServiceImpl<RiskMapper, Risk> {
    public List<Risk> getRisksByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public List<Risk> getRisksByProjectIdList(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public Risk createRisk(Risk risk) {
        save(risk);
        return risk;
    }
}
