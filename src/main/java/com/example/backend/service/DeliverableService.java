package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Deliverable;
import com.example.backend.mapper.DeliverableMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliverableService extends ServiceImpl<DeliverableMapper, Deliverable> {
    public List<Deliverable> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public Deliverable create(Deliverable deliverable) {
        save(deliverable);
        return deliverable;
    }
    
    public Deliverable getDeliverableById(Integer id) {
        return baseMapper.findById(id);
    }
}