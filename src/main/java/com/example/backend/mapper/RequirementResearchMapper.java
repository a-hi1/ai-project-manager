package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.RequirementResearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RequirementResearchMapper extends BaseMapper<RequirementResearch> {
    @Select("SELECT * FROM requirement_research WHERE id = #{id}")
    RequirementResearch selectById(Integer id);
    
    @Select("SELECT * FROM requirement_research WHERE project_id = #{projectId}")
    List<RequirementResearch> findByProjectId(Integer projectId);
}
