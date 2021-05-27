package com.gitdatsanvich.sweethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.sweethome.model.dto.RegisterDTO;
import com.gitdatsanvich.sweethome.model.entity.User;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author TangChen
 * @since 2021-05-25
 */
public interface UserService extends IService<User> {
    /**
     * 注册
     *
     * @param registerDTO registerDTO
     */
    void registered(RegisterDTO registerDTO) throws BizException;
}
