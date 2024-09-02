package org.dromara.throughproxy.server.proxy.security;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author: yp
 * @date: 2024/9/2 14:10
 * @description:IP,端口安全组校验
 */
@Slf4j
public class UdpVisitorSecurityChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
        int port = address.getPort();

        // TODO：校验IP是否在该端口绑定的安全组允许的规则内

        super.channelRead(ctx, msg);
    }

}
