package org.yp.throughproxy.server.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Component;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.ProxyMessageHandler;
import org.yp.throughproxy.core.dispatcher.Match;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @program: through-proxy
 * @description: 处理被代理客户端发送来的udp消息
 * @author: yp
 * @create: 2024-09-04 21:00
 **/
@Slf4j
@Match(type = Constants.ProxyDataTypeName.UDP_TRANSFER)
@Component
public class UdpTransferProxyMessageHandler implements ProxyMessageHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        log.info("收到来自被代理服务器的udp消息");
        final ProxyMessage.UdpBaseInfo udpBaseInfo = ONode.deserialize(proxyMessage.getInfo(), ProxyMessage.UdpBaseInfo.class);

        Channel channel = ctx.channel();
        Channel visitorChannel = channel.attr(Constants.NEXT_CHANNEL).get();
        if(Objects.isNull(visitorChannel)){
            log.info("外部未与代理服务器进行连接");
            return;
        }

        InetSocketAddress address = ctx.channel().attr(Constants.SENDER).get();
        if(null != address){
            visitorChannel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(proxyMessage.getData()), address));
        }

        //TODO：流量计数

    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.UDP_TRANSFER.getName();
    }
}
