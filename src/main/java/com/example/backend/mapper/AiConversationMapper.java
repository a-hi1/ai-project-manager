package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
    @Select("SELECT * FROM ai_conversation WHERE project_id = #{projectId} ORDER BY created_at ASC")
    List<AiConversation> findByProjectId(@Param("projectId") Integer projectId);
}
