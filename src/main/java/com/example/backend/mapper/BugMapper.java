package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Bug;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BugMapper extends BaseMapper<Bug> {
    @Select("SELECT * FROM bug WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<Bug> findByProjectId(@Param("projectId") Integer projectId);

    @Select("SELECT * FROM bug WHERE assignee_id = #{userId} ORDER BY created_at DESC")
    List<Bug> findByAssigneeId(@Param("userId") Integer userId);
}