package org.dromara.throughproxy.server.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.ProxyDataTypeEnum;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.core.ProxyMessageHandler;
import org.dromara.throughproxy.core.dispatcher.Match;
import org.noear.solon.annotation.Component;

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
