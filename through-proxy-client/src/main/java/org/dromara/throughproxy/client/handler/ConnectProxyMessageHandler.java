package org.dromara.throughproxy.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.client.config.ProxyConfig;
import org.dromara.throughproxy.client.core.ProxyChannelBorrowListener;
import org.dromara.throughproxy.client.util.ProxyUtil;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.ProxyDataTypeEnum;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.core.ProxyMessageHandler;
import org.dromara.throughproxy.core.dispatcher.Match;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * @author: yp
 * @date: 2024/9/3 10:29
 * @description:对应外部穿透到内网，与内网服务进行连接
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.CONNECT)
@Component
public class ConnectProxyMessageHandler implements ProxyMessageHandler {

    @Inject("realServerBootstrap")
    private Bootstrap realServerBootstrap;

    @Inject("tcpProxyTunnelBootstrap")
    private Bootstrap tcpProxyTunnelBootstrap;

    @Inject
    private ProxyConfig proxyConfig;


    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        final Channel cmdChannel = ctx.channel();
        final String visitorId = proxyMessage.getInfo();
        // clientIp:clientPort
        String[] serverInfo = new String(proxyMessage.getData()).split(":");
        String ip = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);

        // 连接真实的，被代理的服务
        realServerBootstrap.connect(ip, port)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {

                            log.info("连接内网服务成功 ip {} port：{}", ip, port);

                            Channel realServerChannel = channelFuture.channel();

                            realServerChannel.config().setOption(ChannelOption.AUTO_READ, false);

                            // 获取连接
                            ProxyUtil.borrowTcpProxyChannel(tcpProxyTunnelBootstrap, new ProxyChannelBorrowListener() {
                                @Override
                                public void success(Channel channel) {
                                    // 设置tcp与被代理连接之间的绑定
                                    channel.attr(Constants.NEXT_CHANNEL).set(realServerChannel);
                                    realServerChannel.attr(Constants.NEXT_CHANNEL).set(channel);

                                    // 远程绑定
                                    channel.writeAndFlush(ProxyMessage.buildConnectMessage(visitorId + "@" + proxyConfig.getTunnel().getLicenseKey()));

                                    realServerChannel.config().setOption(ChannelOption.AUTO_READ, true);
                                    ProxyUtil.addRealServerChannel(visitorId, realServerChannel);
                                    ProxyUtil.setRealServerChannelVisitor(realServerChannel, visitorId);
                                }

                                @Override
                                public void error(Throwable cause) {
                                    ProxyMessage proxyMessage = new ProxyMessage();
                                    proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
                                    proxyMessage.setInfo(visitorId);
                                    cmdChannel.writeAndFlush(proxyMessage);
                                }
                            });

                        } else {
                            // 建立连接失败，发送通知
                            cmdChannel.writeAndFlush(ProxyMessage.buildDisconnectMessage(visitorId));
                        }
                    }
                });
    }


    @Override
    public String name() {
        return ProxyDataTypeEnum.CONNECT.getName();
    }
}
