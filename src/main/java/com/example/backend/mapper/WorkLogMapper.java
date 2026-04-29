package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.WorkLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkLogMapper extends BaseMapper<WorkLog> {
    @Select("SELECT * FROM work_log WHERE task_id = #{taskId} ORDER BY work_date DESC")
    List<WorkLog> findByTaskId(@Param("taskId") Integer taskId);

    @Select("SELECT * FROM work_log WHERE user_id = #{userId} ORDER BY work_date DESC")
    List<WorkLog> findByUserId(@Param("userId") Integer userId);
}
