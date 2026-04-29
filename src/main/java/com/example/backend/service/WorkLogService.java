package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.WorkLog;
import com.example.backend.mapper.WorkLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkLogService extends ServiceImpl<WorkLogMapper, WorkLog> {
    public List<WorkLog> getLogsByTaskId(Integer taskId) {
        return baseMapper.findByTaskId(taskId);
    }

    public List<WorkLog> getLogsByUserId(Integer userId) {
        return baseMapper.findByUserId(userId);
    }

    public WorkLog createLog(WorkLog log) {
        save(log);
        return log;
    }
}