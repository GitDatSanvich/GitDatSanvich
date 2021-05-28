package com.gitdatsanvich.sweethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitdatsanvich.sweethome.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper 接口
 *
 * @author TangChen
 * @since 2021-05-25
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}