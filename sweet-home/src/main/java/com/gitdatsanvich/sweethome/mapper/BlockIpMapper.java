package com.gitdatsanvich.sweethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitdatsanvich.sweethome.model.entity.BlockIp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper 接口
 *
 * @author TangChen
 * @since 2021-06-09
 */
@Mapper
public interface BlockIpMapper extends BaseMapper<BlockIp> {
    boolean isBlack(@Param("ip") String ip, @Param("now") long now);
}
