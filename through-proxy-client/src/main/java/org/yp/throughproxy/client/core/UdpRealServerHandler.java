package org.yp.throughproxy.client.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.yp.throughproxy.client.constant.Constants;
import org.yp.throughproxy.client.util.UdpChannelBindInfo;
import org.yp.throughproxy.core.ProxyMessage;

import java.util.Objects;

/**
 * @program: through-proxy
 * @description: 接受被代理服务的消息
 * @author: yp
 * @create: 2024-09-04 20:52
 **/
@Slf4j
public class UdpRealServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        UdpChannelBindInfo udpChannelBindInfo = ctx.channel().attr(Constants.UDP_CHANNEL_BIND_KEY).get();
        if(Objects.nonNull(udpChannelBindInfo)){
            Channel tunnelChannel = udpChannelBindInfo.getTunnelChannel();
            byte[] data = new byte[datagramPacket.content().readableBytes()];
            datagramPacket.content().readBytes(data);
            tunnelChannel.writeAndFlush(ProxyMessage.buildUdpTransferMessage(new ProxyMessage.UdpBaseInfo()
                            .setVisitorId(udpChannelBindInfo.getVisitorId())
                            .setVisitorIp(udpChannelBindInfo.getVisitorIp())
                            .setVisitorPort(udpChannelBindInfo.getVisitorPort())
                            .setServerPort(udpChannelBindInfo.getServerPort())
                            .setTargetIp(udpChannelBindInfo.getTargetIp())
                            .setTargetPort(udpChannelBindInfo.getTargetPort()))
                    .setData(data)
            );

            udpChannelBindInfo.getLockChannel().setResponseCount(udpChannelBindInfo.getLockChannel().getResponseCount() + 1);
        }
    }

}
