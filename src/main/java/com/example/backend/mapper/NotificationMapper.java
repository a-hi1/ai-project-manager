package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND read_status = false")
    int countUnreadByUserId(Integer userId);

    @Update("UPDATE notification SET read_status = true WHERE user_id = #{userId}")
    void markAllAsRead(Integer userId);
}
