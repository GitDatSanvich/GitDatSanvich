package com.gitdatsanvich.sweethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitdatsanvich.sweethome.mapper.GroupMapper;
import com.gitdatsanvich.sweethome.model.entity.Group;
import com.gitdatsanvich.sweethome.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author TangChen
 * @since 2021-05-25
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    private static Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);
}
