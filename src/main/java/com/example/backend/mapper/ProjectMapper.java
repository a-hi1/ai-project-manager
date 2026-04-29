package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    @Select("SELECT * FROM project WHERE id = #{id}")
    Project selectById(Integer id);

    @Select("SELECT * FROM project ORDER BY created_at DESC")
    List<Project> findAll();

    default IPage<Project> selectPage(Page<Project> page) {
        return selectPage(page, null);
    }

    @Select("SELECT COUNT(*) FROM task WHERE project_id = #{projectId}")
    int countTasksByProjectId(Integer projectId);

    @Select("SELECT COUNT(*) FROM task WHERE project_id = #{projectId} AND status = 'done'")
    int countCompletedTasksByProjectId(Integer projectId);

    @Select("SELECT COUNT(*) FROM risk WHERE project_id = #{projectId}")
    int countRisksByProjectId(Integer projectId);

    @Select("SELECT COUNT(*) FROM bug WHERE project_id = #{projectId}")
    int countBugsByProjectId(Integer projectId);
}
