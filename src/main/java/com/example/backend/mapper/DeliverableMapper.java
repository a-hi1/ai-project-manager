package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Deliverable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeliverableMapper extends BaseMapper<Deliverable> {
    @Select("SELECT * FROM deliverable WHERE project_id = #{projectId} ORDER BY submitted_at DESC")
    List<Deliverable> findByProjectId(@Param("projectId") Integer projectId);
}
