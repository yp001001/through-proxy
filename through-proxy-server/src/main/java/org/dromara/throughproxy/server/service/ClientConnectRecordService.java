package org.dromara.throughproxy.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.dal.ClientConnectRecordMapper;
import org.dromara.throughproxy.server.dal.LicenseMapper;
import org.dromara.throughproxy.server.dal.UserMapper;
import org.dromara.throughproxy.server.dal.entity.ClientConnectRecord;
import org.noear.solon.annotation.Component;

/**
 * @author: yp
 * @date: 2024/9/2 10:57
 * @description:
 */
@Slf4j
@Component
public class ClientConnectRecordService {

    @Db
    private ClientConnectRecordMapper clientConnectRecordMapper;
    @Db
    private LicenseMapper licenseMapper;
    @Db
    private UserMapper userMapper;

    public void add(ClientConnectRecord clientConnectRecord) {
        clientConnectRecordMapper.insert(clientConnectRecord);
    }

}
