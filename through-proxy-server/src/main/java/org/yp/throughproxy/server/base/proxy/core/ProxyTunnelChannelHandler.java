package org.yp.throughproxy.server.base.proxy.core;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.dispatcher.Dispatcher;
import org.yp.throughproxy.server.base.proxy.ProxyConfig;
import org.yp.throughproxy.server.base.proxy.domain.CmdChannelAttachInfo;
import org.yp.throughproxy.server.constant.ClientConnectTypeEnum;
import org.yp.throughproxy.server.constant.SuccessCodeEnum;
import org.yp.throughproxy.server.dal.entity.ClientConnectRecord;
import org.yp.throughproxy.server.service.ClientConnectRecordService;
import org.yp.throughproxy.server.util.ProxyUtil;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Objects;


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

    public ProxyTunnelChannelHandler() {
        dispatcher = Solon.context().getBean(Dispatcher.class);
        ProxyConfig proxyConfig = Solon.context().getBean(ProxyConfig.class);
        if (null != proxyConfig.getTunnel() && null != proxyConfig.getTunnel().getHeartbeatLogEnable()) {
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
        if (visitorChannel != null) {
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        Channel visitorChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        // 表示用于被代理端口之间通信的channel断开
        if (Objects.nonNull(visitorChannel)) {

            String visitorId = visitorChannel.attr(Constants.VISITOR_ID).get();
            Integer licenseId = visitorChannel.attr(Constants.LICENSE_ID).get();

            // 获取代理客户端（非Auth）中与代理服务端连接的通信通道
            Channel cmdChannel = ProxyUtil.getCmdChannelByLicenseId(licenseId);

            if(Objects.nonNull(cmdChannel)){
                ProxyUtil.removeVisitorChannelFromCmdChannel(cmdChannel, visitorId);
            }

            ProxyUtil.removeProxyConnectAttachment(visitorId);

            Boolean isUdp = visitorChannel.attr(Constants.IS_UDP_KEY).get();
            if(visitorChannel.isActive() && null == isUdp){
                // 数据发送完成之后再关闭连接，解决http1.0数据传输问题
                visitorChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }

        } else {
            // 此时就应该是cmdChannel断开
            CmdChannelAttachInfo cmdChannelAttachInfo = ProxyUtil.getAttachInfo(ctx.channel());

            if(Objects.nonNull(cmdChannelAttachInfo)){
                Channel cmdChannel = ProxyUtil.getCmdChannelByLicenseId(cmdChannelAttachInfo.getLicenseId());
                // 表示代理客户端与代理服务端断开连接
                if(cmdChannel == ctx.channel()){
                    // 删除关于cmdChannel的所有数据
                    ProxyUtil.removeCmdChannel(cmdChannel);
                    // 删除license
                    ProxyUtil.removeClientIdByLicenseId(cmdChannelAttachInfo.getLicenseId());
                }
            }

            // 即便是因为上述原因断开，断开的日志依然要记录，方便排查问题
            Solon.context().getBean(ClientConnectRecordService.class).add(new ClientConnectRecord()
                    .setIp(((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress())
                    .setLicenseId(cmdChannelAttachInfo.getLicenseId())
                    .setType(ClientConnectTypeEnum.DISCONNECT.getType())
                    .setMsg("")
                    .setCode(SuccessCodeEnum.SUCCESS.getCode())
                    .setCreateTime(new Date())
            );
        }


        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[Tunnel Channel] error", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    if (ctx.channel().isWritable()) {
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
