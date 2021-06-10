package com.gitdatsanvich.sweethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gitdatsanvich.sweethome.model.dto.AccessDTO;
import com.gitdatsanvich.sweethome.model.entity.IpAccess;

/**
 * <p>
 *  服务类
 * </p>
 * @author TangChen
 * @since 2021-06-09
 */
public interface IpAccessService extends IService<IpAccess> {
    AccessDTO access(String ip);
}
