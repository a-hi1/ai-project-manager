package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.FeasibilityAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeasibilityAnalysisMapper extends BaseMapper<FeasibilityAnalysis> {
    @Select("SELECT * FROM feasibility_analysis WHERE id = #{id}")
    FeasibilityAnalysis selectById(Integer id);
    
    @Select("SELECT * FROM feasibility_analysis WHERE project_id = #{projectId}")
    List<FeasibilityAnalysis> findByProjectId(Integer projectId);
}
