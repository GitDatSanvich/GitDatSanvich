package com.gitdatsanvich.sweethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitdatsanvich.sweethome.mapper.UserGroupMapper;
import com.gitdatsanvich.sweethome.model.entity.UserGroup;
import com.gitdatsanvich.sweethome.service.UserGroupService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author TangChen
 * @since 2021-05-25
 */
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService {
    /**
     * 用户组 关系维护
     *
     * @param userId  userId
     * @param groupId groupId
     */
    @Override
    public void insertUserGroupRelationShip(String userId, String groupId) {
        UserGroup userGroup = new UserGroup(userId, groupId);
        this.save(userGroup);
    }
}
