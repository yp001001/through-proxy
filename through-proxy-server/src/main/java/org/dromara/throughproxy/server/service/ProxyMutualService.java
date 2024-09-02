package org.dromara.throughproxy.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.base.proxy.domain.CmdChannelAttachInfo;
import org.dromara.throughproxy.server.constant.OnlineStatusEnum;
import org.dromara.throughproxy.server.dal.LicenseMapper;
import org.dromara.throughproxy.server.dal.PortMappingMapper;
import org.noear.solon.annotation.Component;

import java.util.Date;

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

    /**
     * 绑定服务端端口处理
     * @param attachInfo
     * @param serverPort
     */
    public void bindServerPort(CmdChannelAttachInfo attachInfo, Integer serverPort) {
        Date date = new Date();
        portMappingMapper.updateOnlineStatus(attachInfo.getLicenseId(), serverPort, OnlineStatusEnum.ONLINE.getStatus(), date);
        licenseMapper.updateOnlineStatus(attachInfo.getLicenseId(),  OnlineStatusEnum.ONLINE.getStatus(), date);
        log.info("bind server port licenseId:{},ip:{},serverPort:{}", attachInfo.getLicenseId(), attachInfo.getIp(),  serverPort);
    }
}
