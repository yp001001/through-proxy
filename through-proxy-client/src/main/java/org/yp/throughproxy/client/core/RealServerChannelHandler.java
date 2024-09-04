package org.yp.throughproxy.client.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.yp.throughproxy.client.util.ProxyUtil;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyMessage;

import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/3 14:48
 * @description:处理接收被代理服务器返回的数据
 */
@Slf4j
public class RealServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        Channel realServerChannel = ctx.channel();
        Channel proxyChannel = realServerChannel.attr(Constants.NEXT_CHANNEL).get();

        if(Objects.isNull(proxyChannel)){
            log.info("代理客户端断开连接");
            realServerChannel.close();
            return;
        }

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String visitorId = ProxyUtil.getVisitorIdByRealServerChannel(realServerChannel);
        proxyChannel.writeAndFlush(ProxyMessage.buildTransferMessage(visitorId, bytes));
    }

}
