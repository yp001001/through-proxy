package org.yp.throughproxy.server.proxy.security;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.server.service.PortMappingService;
import org.yp.throughproxy.server.service.SecurityGroupService;

import java.net.InetSocketAddress;

/**
 * @author: yp
 * @date: 2024/9/2 14:10
 * @description:IP,端口安全组校验
 */
@Slf4j
public class UdpVisitorSecurityChannelHandler extends ChannelInboundHandlerAdapter {

    private final SecurityGroupService securityGroupService = Solon.context().getBean(SecurityGroupService.class);
    private final PortMappingService portMappingService = Solon.context().getBean(PortMappingService.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
        int port = address.getPort();

        // 校验IP是否在该端口绑定的安全组允许的规则内
        Integer groupId = portMappingService.getSecurityGroupIdByMappingPort(port);
        if(!securityGroupService.judgeAllow(((DatagramPacket) msg).sender().getAddress().getHostAddress(), groupId)){
            log.error("访问端口不在安全组允许范围内... ip：{}  groupId：{}", ((DatagramPacket) msg).sender().getAddress().getHostAddress(), groupId);
            ctx.channel().close();
            return;
        }

        // 继续传播
        ctx.channel().attr(Constants.SERVER_PORT).set(port);
        super.channelRead(ctx, msg);
    }

}
