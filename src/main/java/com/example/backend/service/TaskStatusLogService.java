package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.TaskStatusLog;
import com.example.backend.mapper.TaskStatusLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusLogService extends ServiceImpl<TaskStatusLogMapper, TaskStatusLog> {
    public List<TaskStatusLog> getLogsByTaskId(Integer taskId) {
        return baseMapper.findByTaskId(taskId);
    }

    public TaskStatusLog createLog(TaskStatusLog log) {
        save(log);
        return log;
    }
}