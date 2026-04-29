package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Milestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MilestoneMapper extends BaseMapper<Milestone> {
    @Select("SELECT * FROM milestone WHERE project_id = #{projectId}")
    List<Milestone> findByProjectId(@Param("projectId") Integer projectId);
}
