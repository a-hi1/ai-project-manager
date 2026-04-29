package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ChangeRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChangeRequestMapper extends BaseMapper<ChangeRequest> {
    @Select("SELECT * FROM change_request WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<ChangeRequest> findByProjectId(@Param("projectId") Integer projectId);
}
