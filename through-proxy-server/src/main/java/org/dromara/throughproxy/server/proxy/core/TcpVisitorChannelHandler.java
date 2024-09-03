package org.dromara.throughproxy.server.proxy.core;

import cn.hutool.core.util.StrUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.server.constant.NetworkProtocolEnum;
import org.dromara.throughproxy.server.util.ProxyUtil;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/8/30 18:06
 * @description:处理外部访问
 */
@Slf4j
public class TcpVisitorChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 当外部连接到对应端口，发送消息，让代理客户端连接被代理服务
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
        int serverPort = address.getPort();

        // 将被代理的端口与被代理客户端的channel绑定
        Channel cmdChannel = ProxyUtil.getCmdChannelByServerPort(serverPort);
        if(Objects.isNull(cmdChannel) || !cmdChannel.isActive()){
            log.warn("代理客户端没有连接上代理服务端");
            ctx.channel().close();
            return;
        }

        // 根据代理端服务端口，获取被代理端客户局域网信息
        String lanInfo = ProxyUtil.getClientLanInfoByServerPort(serverPort);
        if(StrUtil.isBlank(lanInfo)){
            log.warn("被代理端ip:port为空");
            ctx.channel().close();
            return;
        }

        // 用户连接到代理端服务器，设置为不可读，等待被代理端连接上代理端
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);

        // 发送消息，代理客户端接收消息连接被代理服务
        String visitorId = ProxyUtil.newVisitorId();
        ProxyUtil.addVisitorChannelToCmdChannel(NetworkProtocolEnum.TCP, cmdChannel, visitorId, ctx.channel(), serverPort);

        cmdChannel.writeAndFlush(ProxyMessage.buildConnectMessage(visitorId).setData(lanInfo.getBytes()));

        super.channelActive(ctx);

    }

    /**
     * 接收到外部访问发送消息
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param byteBuf           the message to handle
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {

        Channel visitorChannel = ctx.channel();
        // 在ConnectProxyMessageHandler中接收到connect请求后会将代理客户端channel保存到visitorChannel
        Channel proxyChannel = visitorChannel.attr(Constants.NEXT_CHANNEL).get();

        if(Objects.isNull(proxyChannel)){
            log.info("该端口暂时还没有代理");
            ctx.channel().close();
            return;
        }

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        // 代理通道可写，则设置访问通道可读，代理通道不可写，则设置访问通道不可读
        visitorChannel.config().setAutoRead(proxyChannel.isWritable());

        // 转发代理数据
        // 在外部连接的时候会将数据保存到visitorChannel中
        String visitorId = ProxyUtil.getVisitorIdByChannel(visitorChannel);
        proxyChannel.writeAndFlush(ProxyMessage.buildTransferMessage(visitorId, bytes));

        // TODO: 增加流量计数
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("VisitorChannel error", cause);
        ctx.close();
    }

}
