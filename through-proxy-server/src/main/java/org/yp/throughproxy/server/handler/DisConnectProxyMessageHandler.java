package org.yp.throughproxy.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.ProxyMessageHandler;
import org.yp.throughproxy.core.dispatcher.Match;

/**
 * @author: yp
 * @date: 2024/9/5 17:20
 * @description:
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.DISCONNECT)
public class DisConnectProxyMessageHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {

        //TODO

        Channel proxyChannel = channelHandlerContext.channel();
        Channel visitorChannel = proxyChannel.attr(Constants.NEXT_CHANNEL).get();
        if (null != visitorChannel) {
            visitorChannel.close();
        }
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.DISCONNECT.getName();
    }
}
