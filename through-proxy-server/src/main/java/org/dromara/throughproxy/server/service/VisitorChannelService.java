package org.dromara.throughproxy.server.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.dal.LicenseMapper;
import org.dromara.throughproxy.server.dal.PortMappingMapper;
import org.dromara.throughproxy.server.dal.PortPoolMapper;
import org.dromara.throughproxy.server.dal.UserMapper;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * @author: yp
 * @date: 2024/8/30 15:27
 * @description:访问者通道服务
 */
@Slf4j
@Component
public class VisitorChannelService {

    @Inject("tcpServerBootstrap")
    private ServerBootstrap tcpServerBootstrap;

    @Inject
    private ProxyMutualService proxyMutualService;

    @Db
    private UserMapper userMapper;

    @Db
    private LicenseMapper licenseMapper;

    @Db
    private PortMappingMapper portMappingMapper;

    @Db
    private PortPoolMapper portPoolMapper;

}
