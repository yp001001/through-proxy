package org.dromara.throughproxy.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.dal.LicenseMapper;
import org.dromara.throughproxy.server.dal.PortMappingMapper;
import org.noear.solon.annotation.Component;

/**
 * @author: yp
 * @date: 2024/8/30 15:28
 * @description:代理交互服务
 */
@Slf4j
@Component
public class ProxyMutualService {
    @Db
    private PortMappingMapper portMappingMapper;
    @Db
    private LicenseMapper licenseMapper;
}
