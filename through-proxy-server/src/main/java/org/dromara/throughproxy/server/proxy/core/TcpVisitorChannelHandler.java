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
import java.net.SocketAddress;
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

        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int serverPort = socketAddress.getPort();

        // 判断端口是否存在被代理客户端
        Channel clientChannel = ProxyUtil.getCmdChannelByServerPort(serverPort);
        if (Objects.isNull(clientChannel)) {
            log.warn("代理服务器端口{} 没有对应的客户端...", serverPort);
            ctx.channel().close();
            return;
        }

        // 保存被代理端与用户端的对应关系
        String proxyClientInfo = ProxyUtil.getClientLanInfoByServerPort(serverPort);
        if (StrUtil.isBlank(proxyClientInfo)) {
            log.info("服务端 端口{} 获取到被代理端消息失败", serverPort);
            ctx.channel().close();
            return;
        }

        // 用户连接到代理服务器，设置用户连接不可读，等待代理后端服务器连接成功之后再改变为可读状态
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);

        String visitorId = ProxyUtil.newVisitorId();
        // todo：暂未实现
        ProxyUtil.addVisitorChannelToCmdChannel(NetworkProtocolEnum.TCP, clientChannel, visitorId, ctx.channel(), serverPort);
        clientChannel.writeAndFlush(ProxyMessage.buildConnectMessage(visitorId).setData(proxyClientInfo.getBytes()));

        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当出现异常就关闭连接
        ctx.close();
        log.error("VisitorChannel error", cause);
    }

}
