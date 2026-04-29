package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Bug;
import com.example.backend.mapper.BugMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BugService extends ServiceImpl<BugMapper, Bug> {
    public List<Bug> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public List<Bug> getByAssigneeId(Integer userId) {
        return baseMapper.findByAssigneeId(userId);
    }

    public Bug create(Bug bug) {
        save(bug);
        return bug;
    }

    public boolean updateById(Bug bug) {
        return super.updateById(bug);
    }

    public boolean removeById(Integer id) {
        return super.removeById(id);
    }
}
