package org.yp.throughproxy.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.yp.throughproxy.server.dal.UserMapper;
import org.yp.throughproxy.server.dal.entity.User;

/**
 * @author: yp
 * @date: 2024/9/3 17:11
 * @description:
 */
@Slf4j
@Component
public class UserService {

    @Db
    private UserMapper userMapper;

    public User queryById(Integer userId) {
        return userMapper.queryById(userId);
    }
}
