package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Risk;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RiskMapper extends BaseMapper<Risk> {
    @Select("SELECT * FROM risk WHERE project_id = #{projectId}")
    List<Risk> findByProjectId(@Param("projectId") Integer projectId);
}