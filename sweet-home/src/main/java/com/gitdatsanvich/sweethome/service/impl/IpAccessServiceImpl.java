package com.gitdatsanvich.sweethome.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitdatsanvich.common.constants.CacheConstants;
import com.gitdatsanvich.sweethome.mapper.IpAccessMapper;
import com.gitdatsanvich.sweethome.model.dto.AccessDTO;
import com.gitdatsanvich.sweethome.model.entity.BlockIp;
import com.gitdatsanvich.sweethome.model.entity.IpAccess;
import com.gitdatsanvich.sweethome.service.BlockIpService;
import com.gitdatsanvich.sweethome.service.IpAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author TangChen
 * @since 2021-06-09
 */
@Service
public class IpAccessServiceImpl extends ServiceImpl<IpAccessMapper, IpAccess> implements IpAccessService {
    private static final Logger logger = LoggerFactory.getLogger(IpAccessServiceImpl.class);

    @Resource
    private BlockIpService blockIpService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Cacheable(value = CacheConstants.BLACK_IP, key = "#ip", unless = "#result.accessAble==true")
    public AccessDTO access(String ip) {
        logger.info("ip访问：" + ip);
        /*查询是否黑名单用户*/
        if (blockIpService.isBlack(ip)) {
            return new AccessDTO(ip, 0, false, 0);
        }
        /*存入访问*/
        long now = System.currentTimeMillis();
        IpAccess ipAccess = new IpAccess(null, ip, now);
        baseMapper.insert(ipAccess);
        /*查询5分钟内是否有100次请求*/
        if (baseMapper.isBlack(ip, now)) {
            blockIpService.save(new BlockIp(null, ip, now));
            return new AccessDTO(ip, null, false, 0);
        }
        return new AccessDTO(ip, this.count(Wrappers.<IpAccess>lambdaQuery().eq(IpAccess::getIp, ip)), true, this.count(Wrappers.lambdaQuery()));
    }
}
