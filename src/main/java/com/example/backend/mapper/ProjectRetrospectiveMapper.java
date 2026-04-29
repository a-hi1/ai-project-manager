package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ProjectRetrospective;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectRetrospectiveMapper extends BaseMapper<ProjectRetrospective> {
    @Select("SELECT * FROM project_retrospective WHERE project_id = #{projectId}")
    List<ProjectRetrospective> findByProjectId(@Param("projectId") Integer projectId);
}
