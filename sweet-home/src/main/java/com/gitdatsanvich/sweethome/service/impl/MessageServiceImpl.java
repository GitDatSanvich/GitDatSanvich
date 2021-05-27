package com.gitdatsanvich.sweethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitdatsanvich.sweethome.mapper.MessageMapper;
import com.gitdatsanvich.sweethome.model.entity.Message;
import com.gitdatsanvich.sweethome.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author TangChen
 * @since 2021-05-25
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    private static Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
}
