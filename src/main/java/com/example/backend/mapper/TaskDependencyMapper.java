package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {
    @Select("SELECT * FROM task_dependency WHERE task_id = #{taskId}")
    List<TaskDependency> findByTaskId(@Param("taskId") Integer taskId);

    @Select("SELECT * FROM task_dependency WHERE depends_on_id = #{dependsOnId}")
    List<TaskDependency> findByDependsOnId(@Param("dependsOnId") Integer dependsOnId);
}
