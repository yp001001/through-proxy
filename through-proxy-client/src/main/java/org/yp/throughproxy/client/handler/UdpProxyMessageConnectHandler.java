package org.yp.throughproxy.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.yp.throughproxy.client.config.ProxyConfig;
import org.yp.throughproxy.client.core.ProxyChannelBorrowListener;
import org.yp.throughproxy.client.util.ProxyUtil;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.ProxyMessageHandler;
import org.yp.throughproxy.core.dispatcher.Match;

/**
 * @author: yp
 * @date: 2024/9/4 13:46
 * @description:
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.UDP_CONNECT)
@Component
public class UdpProxyMessageConnectHandler implements ProxyMessageHandler {

    @Inject
    private ProxyConfig proxyConfig;
    @Inject("udpProxyTunnelBootstrap")
    private Bootstrap udpProxyTunnelBootstrap;

    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        final Channel cmdChannel = ctx.channel();
        final ProxyMessage.UdpBaseInfo udpBaseInfo = ONode.deserialize(proxyMessage.getInfo(), ProxyMessage.UdpBaseInfo.class);
        log.info("[UDP connect]info:{}", proxyMessage.getInfo());

        // 代理服务端与代理客户端之间进行连接
        ProxyUtil.borrowUdpProxyChanel(udpProxyTunnelBootstrap, new ProxyChannelBorrowListener() {
            @Override
            public void success(Channel channel) {

                channel.writeAndFlush(ProxyMessage.buildUdpConnectMessage(new ProxyMessage.UdpBaseInfo()
                        .setVisitorId(udpBaseInfo.getVisitorId())
                        .setServerPort(udpBaseInfo.getServerPort())
                        .setTargetIp(udpBaseInfo.getTargetIp())
                        .setTargetPort(udpBaseInfo.getTargetPort())
                ).setData(proxyConfig.getTunnel().getLicenseKey().getBytes()));
            }

            @Override
            public void error(Throwable cause) {
                cmdChannel.writeAndFlush(ProxyMessage.buildDisconnectMessage(udpBaseInfo.toJsonString()));
            }
        });
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.UDP_CONNECT.getDesc();
    }


}
