package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
    @Select("SELECT * FROM knowledge_document WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<KnowledgeDocument> findByProjectId(@Param("projectId") Integer projectId);
}
