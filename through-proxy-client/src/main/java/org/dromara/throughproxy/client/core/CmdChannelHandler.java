package org.dromara.throughproxy.client.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.client.config.ProxyConfig;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.core.dispatcher.Dispatcher;
import org.noear.solon.Solon;

import java.util.Objects;

/**
 * @program: through-proxy
 * @description: 被代理端处理器
 * @author: yp
 * @create: 2024-08-31 23:26
 **/
@Slf4j
public class CmdChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static volatile Boolean transferLogEnable = Boolean.FALSE;

    public CmdChannelHandler() {
        ProxyConfig proxyConfig = Solon.context().getBean(ProxyConfig.class);
        if (null != proxyConfig.getClient() && null != proxyConfig.getTunnel().getHeartbeatLogEnable()) {
            transferLogEnable = proxyConfig.getTunnel().getHeartbeatLogEnable();
        }
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
        if(ProxyMessage.TYPE_HEARTBEAT != proxyMessage.getType() || transferLogEnable){
            log.debug("[CMD Channel]Client CmdChannel recieved proxy message, type is {}", proxyMessage.getType());
        }
        Solon.context().getBean(Dispatcher.class).dispatch(channelHandlerContext, proxyMessage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO:被代理端断开连接
        log.info("[CMD Channel]Client CmdChannel disconnect");
//        ProxyUtil.setCmdChannel(null);
//        ProxyUtil.clearRealServerChannels();
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[CMD Channel]Client CmdChannel Error channelId:{}", ctx.channel().id().asLongText(), cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realServerChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if(!Objects.isNull(realServerChannel)){
            realServerChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()){
                case READER_IDLE:
                    log.error("[CMD Channel] Read timeout disconnect");
                    ctx.channel().close();
                    break;
                case WRITER_IDLE:
                    // 发送心跳防止读超时
                    ctx.channel().writeAndFlush(ProxyMessage.buildHeartbeatMessage());
                    break;
                default:
                    log.error("[CMD Channel] ReadWrite timeout disconnect");
                    ctx.close();
                    break;
            }
        }
    }
}
