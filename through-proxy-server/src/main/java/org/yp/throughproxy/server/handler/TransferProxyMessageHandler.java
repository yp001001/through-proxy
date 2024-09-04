package org.yp.throughproxy.server.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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
 * @date: 2024/9/3 18:39
 * @description:
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.TRANSFER)
@Component
public class TransferProxyMessageHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        log.info("接收到被代理服务返回的信息：{}", JSON.toJSONString(proxyMessage));

        Channel proxyChannel = ctx.channel();
        Channel visitorChannel = proxyChannel.attr(Constants.NEXT_CHANNEL).get();

        if (null != visitorChannel) {
            ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
            buf.writeBytes(proxyMessage.getData());
            visitorChannel.writeAndFlush(buf);

            // TODO: 增加计数
        }

    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.TRANSFER.name();
    }
}
