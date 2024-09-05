package org.yp.throughproxy.client.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.yp.throughproxy.client.util.ProxyUtil;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.dispatcher.Dispatcher;

import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/3 15:42
 * @description:处理与服务端之间的数据传输
 */
@Slf4j
public class TcpProxyChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws Exception {
        if (ProxyMessage.TYPE_HEARTBEAT != proxyMessage.getType()) {
            log.debug("[TCP Proxy Channel]Client ProxyChannel recieved proxy message, type is {}", proxyMessage.getType());
        }
        Solon.context().getBean(Dispatcher.class).dispatch(ctx, proxyMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[TCP Proxy Channel]Client ProxyChannel Error channelId:{}", ctx.channel().id().asLongText(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        Channel realServerChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if(Objects.nonNull(realServerChannel)){
            realServerChannel.close();
        }

        ProxyUtil.removeTcpProxyChannel(ctx.channel());

        // todo: ???
        ctx.channel().close();

        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        // TODO
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            switch (event.state()) {
                case READER_IDLE:
                    if (ctx.channel().isWritable()) {
                        // 读超时，断开连接
                        log.info("[TCP Proxy Channel]Read timeout");
                        ctx.channel().close();
                    }
                    break;
                case WRITER_IDLE:
                    ctx.channel().writeAndFlush(ProxyMessage.buildHeartbeatMessage());
                    break;
                case ALL_IDLE:
//                    log.debug("[TCP Proxy Channel]ReadWrite timeout");
//                    ctx.close();
                    break;
            }
        }
    }

}

