package org.yp.throughproxy.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Component;
import org.yp.throughproxy.client.util.UdpServerUtil;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.ProxyMessageHandler;
import org.yp.throughproxy.core.dispatcher.Match;

import java.net.InetSocketAddress;
import java.util.Objects;


/**
 * @author: yp
 * @date: 2024/9/4 15:24
 * @description:处理被代理端与代理客户端之间的消息通信
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.UDP_TRANSFER)
@Component
public class UdpProxyMessageTransaferHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        final ProxyMessage.UdpBaseInfo udpBaseInfo = ONode.deserialize(proxyMessage.getInfo(), ProxyMessage.UdpBaseInfo.class);
        log.debug("[UDP transfer]info:{} data:{}", proxyMessage.getInfo(), new String(proxyMessage.getData()));

        // 获取udp连接，并且将ctx.channel保存到udp通道中
        Channel udpChannel = UdpServerUtil.takeChannel(udpBaseInfo, ctx.channel());
        if (Objects.isNull(udpChannel)) {
            log.error("[UDP transfer] take udp channel failed.");
            return;
        }

        log.info("udp协议发送数据：localPort：{} remotePort：{}", ((InetSocketAddress)udpChannel.localAddress()).getPort(), udpBaseInfo.getTargetPort());
        InetSocketAddress address = new InetSocketAddress(udpBaseInfo.getTargetIp(), udpBaseInfo.getTargetPort());
        ByteBuf buf = Unpooled.copiedBuffer(proxyMessage.getData());
        udpChannel.writeAndFlush(new DatagramPacket(buf, address));
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.UDP_TRANSFER.getDesc();
    }
}
