package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Task;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {
    @Select("SELECT * FROM task WHERE project_id = #{projectId} ORDER BY path")
    List<Task> findByProjectId(@Param("projectId") Integer projectId);

    @Select("SELECT * FROM task WHERE parent_id = #{parentId}")
    List<Task> findByParentId(@Param("parentId") Integer parentId);

    @Select("SELECT * FROM task WHERE project_id = #{projectId} AND is_milestone = true")
    List<Task> findMilestonesByProjectId(@Param("projectId") Integer projectId);
}