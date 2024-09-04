package org.yp.throughproxy.server.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.ProxyMessageHandler;
import org.yp.throughproxy.core.dispatcher.Match;

/**
 * @author: yp
 * @date: 2024/9/2 17:14
 * @description:
 */
@Slf4j
@Component
@Match(type = Constants.ProxyDataTypeName.HEARTBEAT)
public class HeartbeatProxyMessageHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
        channelHandlerContext.channel().writeAndFlush(ProxyMessage.buildHeartbeatMessage());
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.HEARTBEAT.getDesc();
    }

}
