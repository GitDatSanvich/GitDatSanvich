package com.gitdatsanvich.sweethome.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.sweethome.mapper.UserMapper;
import com.gitdatsanvich.sweethome.model.dto.RegisterDTO;
import com.gitdatsanvich.sweethome.model.entity.Group;
import com.gitdatsanvich.sweethome.model.entity.User;
import com.gitdatsanvich.sweethome.service.GroupService;
import com.gitdatsanvich.sweethome.service.UserGroupService;
import com.gitdatsanvich.sweethome.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author TangChen
 * @since 2021-05-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Resource
    private GroupService groupService;
    @Resource
    private UserGroupService userGroupService;
    /**
     * 注册
     *
     * @param registerDTO registerDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registered(RegisterDTO registerDTO) throws BizException {
        String userId = initUser(registerDTO);
        String groupId = initGroup(registerDTO);
        userGroupService.insertUserGroupRelationShip(userId, groupId);
    }

    private String initGroup(RegisterDTO registerDTO) {
        String groupName = registerDTO.getGroupName();
        Group one = groupService.getOne(Wrappers.<Group>lambdaQuery().eq(Group::getGroupName, groupName));
        if (one == null) {
            Group group = new Group(groupName);
            groupService.save(group);
            return group.getId();
        }
        return one.getId();
    }

    private String initUser(RegisterDTO registerDTO) throws BizException {
        int count = this.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, registerDTO.getUserName()));
        if (count > 0) {
            throw BizException.USER_INFO_EXCEPTION.newInstance("登录用户名已经被别人占用了哦");
        }
        logger.info("注册" + registerDTO.getUserName());
        User user = new User(registerDTO.getUserName(), registerDTO.getPassword(), registerDTO.getName(), registerDTO.getEmail());
        this.save(user);
        return user.getId();
    }
}
