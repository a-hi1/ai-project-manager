package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ProjectMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
    @Select("SELECT * FROM project_member WHERE id = #{id}")
    ProjectMember selectById(Integer id);

    @Select("SELECT * FROM project_member WHERE project_id = #{projectId}")
    List<ProjectMember> findByProjectId(Integer projectId);

    @Select("SELECT * FROM project_member WHERE user_id = #{userId}")
    List<ProjectMember> findByUserId(Integer userId);

    @Delete("DELETE FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    void deleteByProjectAndUser(Integer projectId, Integer userId);
}
