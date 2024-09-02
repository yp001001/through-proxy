package org.dromara.throughproxy.server.proxy.core;

import cn.hutool.core.util.StrUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.server.constant.NetworkProtocolEnum;
import org.dromara.throughproxy.server.util.ProxyUtil;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/8/30 18:06
 * @description:
 */
@Slf4j
public class TcpVisitorChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        int serverPort = address.getPort();

        Channel cmdChannel = ProxyUtil.getCmdChannelByServerPort(serverPort);
        if(Objects.isNull(cmdChannel) || !cmdChannel.isActive()){
            log.warn("被代理端没有连接上代理端");
            ctx.channel().close();
            return;
        }

        // 根据代理端服务端口，获取被代理端客户局域网信息
        String lanInfo = ProxyUtil.getClientLanInfoByServerPort(serverPort);
        if(StrUtil.isBlank(lanInfo)){
            log.warn("被代理端没有连接上代理端");
            ctx.channel().close();
            return;
        }

        // 用户连接到代理端服务器，设置为不可读，等待被代理端连接上代理端
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);

        // 发送消息，被代理端接收消息连接代理端
        String visitorId = ProxyUtil.newVisitorId();
        ProxyUtil.addVisitorChannelToCmdChannel(NetworkProtocolEnum.TCP, cmdChannel, visitorId, ctx.channel(), serverPort);

        cmdChannel.writeAndFlush(ProxyMessage.buildConnectMessage(visitorId).setData(lanInfo.getBytes()));

        super.channelActive(ctx);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("VisitorChannel error", cause);
        ctx.close();
    }

}
