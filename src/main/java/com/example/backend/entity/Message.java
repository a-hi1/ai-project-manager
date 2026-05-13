package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private String type;
    private Boolean readStatus;
    private String replyTo;
    private LocalDateTime createTime;
}