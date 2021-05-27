package com.gitdatsanvich.sweethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.sweethome.model.entity.UserGroup;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author TangChen
 * @since 2021-05-25
 */
public interface UserGroupService extends IService<UserGroup> {
    /**
     * 用户组 关系维护
     *
     * @param userId  userId
     * @param groupId groupId
     */
    void insertUserGroupRelationShip(String userId, String groupId);
}
