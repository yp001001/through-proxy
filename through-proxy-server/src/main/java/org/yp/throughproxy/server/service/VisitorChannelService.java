package org.yp.throughproxy.server.service;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.yp.throughproxy.server.base.proxy.domain.CmdChannelAttachInfo;
import org.yp.throughproxy.server.constant.EnableStatusEnum;
import org.yp.throughproxy.server.constant.NetworkProtocolEnum;
import org.yp.throughproxy.server.dal.LicenseMapper;
import org.yp.throughproxy.server.dal.PortMappingMapper;
import org.yp.throughproxy.server.dal.PortPoolMapper;
import org.yp.throughproxy.server.dal.UserMapper;
import org.yp.throughproxy.server.dal.entity.PortMapping;
import org.yp.throughproxy.server.proxy.domain.ProxyMapping;
import org.yp.throughproxy.server.util.ProxyUtil;

import java.net.BindException;
import java.util.List;
import java.util.stream.Collectors;

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
    @Inject("udpServerBootstrap")
    private Bootstrap udpServerBootstrap;

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

    /**
     * 初始化
     *
     * @param licenseId
     * @param cmdChannel
     */
    public void initVisitorChannel(int licenseId, Channel cmdChannel) {
        List<PortMapping> portMappingList = portMappingMapper.findEnableListByLicenseId(licenseId);
        // 保存licenseId对应的服务端端口映射，以及serverPort和对应的客户端地址
        ProxyUtil.initProxyInfo(licenseId, ProxyMapping.buildList(portMappingList));

        ProxyUtil.addCmdChannel(licenseId, cmdChannel, portMappingList.stream().map(PortMapping::getServerPort).collect(Collectors.toSet()));
        startUserPortServer(ProxyUtil.getAttachInfo(cmdChannel), portMappingList);
    }

    private void startUserPortServer(CmdChannelAttachInfo cmdChannelAttachInfo, List<PortMapping> portMappingList) {
        if(CollectionUtil.isEmpty(portMappingList)){
            return;
        }

        for (PortMapping portMapping : portMappingList) {
            if(EnableStatusEnum.DISABLE.getStatus().equals(portMapping.getEnable())){
                continue;
            }
            try{
                proxyMutualService.bindServerPort(cmdChannelAttachInfo, portMapping.getServerPort());

                NetworkProtocolEnum networkProtocolEnum = NetworkProtocolEnum.of(portMapping.getProtocol());
                if (networkProtocolEnum == NetworkProtocolEnum.UDP) {
                    udpServerBootstrap.bind(portMapping.getServerPort()).get();
                    log.info("bind UDP user port： {}", portMapping.getServerPort());
                } else {
                    tcpServerBootstrap.bind(portMapping.getServerPort()).get();
                    log.info("bind TCP user port： {}", portMapping.getServerPort());
                }
            }catch (Exception ex){
                // BindException表示该端口已经绑定过
                if (!(ex.getCause() instanceof BindException)) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
