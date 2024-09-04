package org.yp.throughproxy.client.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.dispatcher.Dispatcher;

/**
 * @author: yp
 * @date: 2024/9/4 13:55
 * @description:处理外部之间的数据传输
 */
@Slf4j
public class UdpProxyChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws Exception {
        if (ProxyMessage.TYPE_HEARTBEAT != proxyMessage.getType()) {
            log.debug("[UDP Proxy Channel]Client ProxyChannel recieved proxy message, type is {}", proxyMessage.getType());
        }
        Solon.context().getBean(Dispatcher.class).dispatch(ctx, proxyMessage);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[UDP Proxy Channel]Client ProxyChannel Error channelId:{}", ctx.channel().id().asLongText(), cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            switch (event.state()) {
                case READER_IDLE:
                    // 读超时，断开连接
                    log.info("[UDP Proxy Channel]Read timeout");
                    ctx.channel().close();
                    break;
                case WRITER_IDLE:
                    ctx.channel().writeAndFlush(ProxyMessage.buildHeartbeatMessage());
                    break;
                case ALL_IDLE:
                    log.debug("[UDP Proxy Channel]ReadWrite timeout");
                    ctx.close();
                    break;
            }
        }
    }
}
