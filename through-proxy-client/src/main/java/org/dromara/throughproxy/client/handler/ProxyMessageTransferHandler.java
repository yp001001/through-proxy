package org.dromara.throughproxy.client.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.ProxyDataTypeEnum;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.core.ProxyMessageHandler;
import org.dromara.throughproxy.core.dispatcher.Match;
import org.noear.solon.annotation.Component;

import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/3 18:20
 * @description:接受來自外部請求，经过代理服务器转发来的数据
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.TRANSFER)
@Component
public class ProxyMessageTransferHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        log.info("接收到外部访问的信息：{}", JSON.toJSONString(proxyMessage));

        Channel clientChannel = ctx.channel();
        Channel realServerChannel = clientChannel.attr(Constants.NEXT_CHANNEL).get();

        if (Objects.isNull(realServerChannel)) {
            log.warn("服务暂时没有被代理");
            return;
        }

        // 自己可写，则设置来源可读。自己不可写，则设置来源不可读
        ctx.channel().config().setAutoRead(realServerChannel.isWritable());

        // 被代理服务器没有编解码器，传输bytebuf
        ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
        buf.writeBytes(proxyMessage.getData());
        realServerChannel.writeAndFlush(buf);
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.TRANSFER.getName();
    }
}


