package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Select("SELECT * FROM operation_log ORDER BY create_time DESC LIMIT 100")
    List<OperationLog> findRecentLogs();

    @Select("SELECT * FROM operation_log WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<OperationLog> findByUserId(Integer userId);
}
