package com.gitdatsanvich.sweethome.model.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author TangChen
 * @since 2021-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends Model<Message> {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 内容
     */
    private String message;

    /**
     * 生成时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 生成数据的人
     */
    private String createUserId;

    /**
     * 更新的人
     */
    private String updateUserId;
}
