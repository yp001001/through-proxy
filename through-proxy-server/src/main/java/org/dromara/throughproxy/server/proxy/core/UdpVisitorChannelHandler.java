package org.dromara.throughproxy.server.proxy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

/**
 * @author: yp
 * @date: 2024/9/2 14:23
 * @description:
 */
@Slf4j
public class UdpVisitorChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // TODO
    }
}
