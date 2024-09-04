package org.yp.throughproxy.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.yp.throughproxy.core.*;
import org.yp.throughproxy.core.dispatcher.Match;
import org.yp.throughproxy.server.constant.EnableStatusEnum;
import org.yp.throughproxy.server.dal.entity.License;
import org.yp.throughproxy.server.dal.entity.PortMapping;
import org.yp.throughproxy.server.dal.entity.User;
import org.yp.throughproxy.server.proxy.domain.ProxyAttachment;
import org.yp.throughproxy.server.service.LicenseService;
import org.yp.throughproxy.server.service.PortMappingService;
import org.yp.throughproxy.server.service.UserService;
import org.yp.throughproxy.server.util.ProxyUtil;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/4 14:08
 * @description:
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.UDP_CONNECT)
@Component
public class UdpProxyMessageConnectHandler implements ProxyMessageHandler {

    @Inject
    private LicenseService licenseService;
    @Inject
    private UserService userService;
    @Inject
    private PortMappingService portMappingService;


    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        final ProxyMessage.UdpBaseInfo udpBaseInfo = ONode.deserialize(proxyMessage.getInfo(),ProxyMessage.UdpBaseInfo.class);
        final String licenseKey = new String(proxyMessage.getData());
        log.info("[UDP connect]info:{} licenseKey:{}", proxyMessage.getInfo(), licenseKey);

        License license = licenseService.queryByKey(licenseKey);
        if(Objects.isNull(license) || EnableStatusEnum.DISABLE.getStatus().equals(license.getEnable())){
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "licenseKey is invialid"));
            ctx.channel().close();
            return;
        }

        User user = userService.queryById(license.getUserId());
        if(Objects.isNull(user) || EnableStatusEnum.DISABLE.getStatus().equals(user.getEnable())){
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "user is invialid"));
            ctx.channel().close();
            return;
        }

        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();

        // 保存了服务映射端口与cmdChannel之间的关系
        Channel cmdChannel = ProxyUtil.getCmdChannelByServerPort(udpBaseInfo.getServerPort());
        if(Objects.isNull(cmdChannel)){
            // 表示代理客户端没有连接到代理服务端
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "server error，cmd channel notfound!"));
            ctx.channel().close();
            return;
        }

        Channel visitorChannel = ProxyUtil.getVisitorChannel(cmdChannel, udpBaseInfo.getVisitorId());
        if(Objects.isNull(visitorChannel)){
            return;
        }

        PortMapping portMapping = portMappingService.queryByLicenseIdAndServerPort(license.getId(), udpBaseInfo.getServerPort());
        if (null == portMapping || !EnableStatusEnum.ENABLE.getStatus().equals(portMapping.getEnable())) {
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "server error, port mapping notfound!"));
            ctx.channel().close();
            return;
        }

        ctx.channel().attr(Constants.VISITOR_ID).set(udpBaseInfo.getVisitorId());
        ctx.channel().attr(Constants.LICENSE_ID).set(license.getId());
        ctx.channel().attr(Constants.NEXT_CHANNEL).set(visitorChannel);
        ctx.channel().attr(Constants.TARGET_IP).set(portMapping.getClientIp());
        ctx.channel().attr(Constants.TARGET_PORT).set(portMapping.getClientPort());
        ctx.channel().attr(Constants.PROXY_RESPONSES).set(portMapping.getProxyResponses());
        ctx.channel().attr(Constants.PROXY_TIMEOUT_MS).set(portMapping.getProxyTimeoutMs());
        visitorChannel.attr(Constants.NEXT_CHANNEL).set(ctx.channel());

        ProxyAttachment proxyAttachment = ProxyUtil.getProxyConnectAttachment(udpBaseInfo.getVisitorId());
        if (null != proxyAttachment) {
            // 及时释放
            ProxyUtil.removeProxyConnectAttachment(udpBaseInfo.getVisitorId());
            proxyAttachment.execute();
        }
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.UDP_CONNECT.getName();
    }
}
