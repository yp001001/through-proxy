package org.yp.throughproxy.server.base.proxy.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.dispatcher.Dispatcher;
import org.yp.throughproxy.server.base.proxy.ProxyConfig;


/**
 * @program: through-proxy
 * @description: 消息处理器
 * @author: yp
 * @create: 2024-08-31 17:54
 **/

@Slf4j
public class ProxyTunnelChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private final Dispatcher dispatcher;

    private volatile boolean transferLogEnable = Boolean.FALSE;

    public ProxyTunnelChannelHandler(){
        dispatcher = Solon.context().getBean(Dispatcher.class);
        ProxyConfig proxyConfig = Solon.context().getBean(ProxyConfig.class);
        if(null != proxyConfig.getTunnel() && null != proxyConfig.getTunnel().getHeartbeatLogEnable()){
            transferLogEnable = proxyConfig.getTunnel().getHeartbeatLogEnable();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
        if (ProxyMessage.TYPE_HEARTBEAT != proxyMessage.getType() || transferLogEnable) {
            log.debug("Server CmdChannel recieved proxy message, type is {}", proxyMessage.getType());
        }
        dispatcher.dispatch(channelHandlerContext, proxyMessage);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if(visitorChannel != null){
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // todo 断开连接等待实现
        // 当代理客户端断开，但是ProxyUtil中的数据还存在，此时就发送不了数据...
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[Tunnel Channel] error", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()){
                case READER_IDLE:
                    if(ctx.channel().isWritable()){
                        // 读超时，断开连接
                        log.warn("[Tunnel Channel]Read timeout");
                        ctx.channel().close();
                    }
                    break;
                case WRITER_IDLE:
                    ctx.channel().writeAndFlush(ProxyMessage.buildHeartbeatMessage());
                    break;
                default:
                    break;
            }
        }
    }
}
