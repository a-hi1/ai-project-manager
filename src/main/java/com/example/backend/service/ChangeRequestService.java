package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.ChangeRequest;
import com.example.backend.mapper.ChangeRequestMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChangeRequestService extends ServiceImpl<ChangeRequestMapper, ChangeRequest> {
    public List<ChangeRequest> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public ChangeRequest create(ChangeRequest changeRequest) {
        save(changeRequest);
        return changeRequest;
    }
}