package org.yp.throughproxy.client.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.noear.solon.annotation.Component;
import org.yp.throughproxy.client.util.ProxyUtil;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.ProxyMessageHandler;
import org.yp.throughproxy.core.dispatcher.Match;

import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/5 14:05
 * @description:
 */
@Match(type = Constants.ProxyDataTypeName.DISCONNECT)
@Component
public class DisConnectProxyMessageHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
        // 找到被代理channel
        Channel realServerChannel = channelHandlerContext.channel().attr(Constants.NEXT_CHANNEL).get();

        if(Objects.nonNull(realServerChannel)){
            ProxyUtil.returnTcpProxyChannel(channelHandlerContext.channel());
            realServerChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.DISCONNECT.getName();
    }
}
