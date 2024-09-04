package org.yp.throughproxy.core;

import io.netty.channel.ChannelHandlerContext;
import org.yp.throughproxy.core.dispatcher.Handler;

/**
 * @author: yp
 * @date: 2024/8/30 9:26
 * @description:消息处理接口
 */
public interface ProxyMessageHandler extends Handler<ChannelHandlerContext, ProxyMessage> {
}
