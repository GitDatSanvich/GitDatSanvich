package com.gitdatsanvich.sweethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gitdatsanvich.sweethome.model.entity.BlockIp;

/**
 * <p>
 *  服务类
 * </p>
 * @author TangChen
 * @since 2021-06-09
 */
public interface BlockIpService extends IService<BlockIp> {

    boolean isBlack(String ip);
}
