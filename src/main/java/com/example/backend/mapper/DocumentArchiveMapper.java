package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.DocumentArchive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentArchiveMapper extends BaseMapper<DocumentArchive> {
    @Select("SELECT * FROM document_archive WHERE project_id = #{projectId} ORDER BY archived_at DESC")
    List<DocumentArchive> findByProjectId(@Param("projectId") Integer projectId);
}
