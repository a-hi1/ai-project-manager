package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.TaskStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskStatusLogMapper extends BaseMapper<TaskStatusLog> {
    @Select("SELECT * FROM task_status_log WHERE task_id = #{taskId} ORDER BY changed_at DESC")
    List<TaskStatusLog> findByTaskId(@Param("taskId") Integer taskId);
}
