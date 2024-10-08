package org.yp.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yp.throughproxy.server.dal.entity.User;

/**
 * @author: yp
 * @date: 2024/8/30 15:22
 * @description:
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    default User queryById(Integer userId){
        return this.selectById(userId);
    }
}
