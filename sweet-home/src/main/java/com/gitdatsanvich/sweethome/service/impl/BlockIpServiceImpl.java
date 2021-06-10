package com.gitdatsanvich.sweethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitdatsanvich.common.constants.CommonConstants;
import com.gitdatsanvich.sweethome.mapper.BlockIpMapper;
import com.gitdatsanvich.sweethome.model.entity.BlockIp;
import com.gitdatsanvich.sweethome.service.BlockIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author TangChen
 * @since 2021-06-09
 */
@Service
public class BlockIpServiceImpl extends ServiceImpl<BlockIpMapper, BlockIp> implements BlockIpService {
    private static Logger logger = LoggerFactory.getLogger(BlockIpServiceImpl.class);

    /**
     * 若被锁定两个小时不允许登录系统
     *
     * @param ip ip
     * @return flag
     */
    @Override
    public boolean isBlack(String ip) {
        long now = System.currentTimeMillis();
        return this.baseMapper.isBlack(ip, now);
    }
}
