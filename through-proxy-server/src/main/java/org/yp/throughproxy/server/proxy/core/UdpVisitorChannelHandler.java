package org.yp.throughproxy.server.proxy.core;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.server.constant.NetworkProtocolEnum;
import org.yp.throughproxy.server.proxy.domain.ProxyAttachment;
import org.yp.throughproxy.server.util.ProxyUtil;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/2 14:23
 * @description:
 */
@Slf4j
public class UdpVisitorChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        Channel visitorChannel = ctx.channel();
        InetSocketAddress localAddress = (InetSocketAddress) visitorChannel.localAddress();

        byte[] bytes = new byte[datagramPacket.content().readableBytes()];
        datagramPacket.content().readBytes(bytes);
        datagramPacket.content().resetReaderIndex();
        ProxyAttachment proxyAttachment = new ProxyAttachment(ctx.channel(), bytes, ((channel, buf) -> {
            Channel proxyChannel = channel.attr(Constants.NEXT_CHANNEL).get();
            if(Objects.isNull(proxyChannel)){
                // 该端口还未被代理
                ctx.channel().close();
                return;
            }

            proxyChannel.attr(Constants.SENDER).set(datagramPacket.sender());
            String targetIp = proxyChannel.attr(Constants.TARGET_IP).get();
            int targetPort = proxyChannel.attr(Constants.TARGET_PORT).get();
            Integer proxyResponses = proxyChannel.attr(Constants.PROXY_RESPONSES).get();
            Long proxyTimeoutMs = proxyChannel.attr(Constants.PROXY_TIMEOUT_MS).get();

            // 转发代理数据
            String visitorId = ProxyUtil.getVisitorIdByChannel(channel);
            proxyChannel.writeAndFlush(ProxyMessage.buildUdpTransferMessage(new ProxyMessage.UdpBaseInfo()
                    .setVisitorIp(datagramPacket.sender().getAddress().getHostAddress())
                            .setVisitorId(visitorId)
                            .setVisitorPort(datagramPacket.sender().getPort())
                            .setTargetIp(targetIp)
                            .setTargetPort(targetPort)
                            .setProxyTimeoutMs(proxyTimeoutMs)
                            .setProxyResponses(proxyResponses))
                    .setData(bytes));

        }));

        Channel proxyChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if(!Objects.isNull(proxyChannel)){
            // UDP代理隧道已就绪，直接转发
            proxyAttachment.execute();
            return;
        }

        Channel cmdChannel = ProxyUtil.getCmdChannelByServerPort(localAddress.getPort());

        if(Objects.isNull(cmdChannel)){
            // 该端口还没有代理客户端
            visitorChannel.close();
            return;
        }

        // 根据代理服务端端口，获取被代理客户端局域网连接信息
        String lanInfo = ProxyUtil.getClientLanInfoByServerPort(localAddress.getPort());
        if(StrUtil.isBlank(lanInfo)){
            ctx.channel().close();
            return;
        }

        String[] targetInfo = lanInfo.split(":");
        String targetIp = targetInfo[0];
        int targetPort = Integer.parseInt(targetInfo[1]);

        String visitorId = ProxyUtil.newVisitorId();
        ProxyUtil.addVisitorChannelToCmdChannel(NetworkProtocolEnum.UDP, cmdChannel, visitorId, visitorChannel, localAddress.getPort());
        ProxyUtil.addProxyConnectAttachment(visitorId, proxyAttachment);
        cmdChannel.writeAndFlush(ProxyMessage.buildUdpConnectMessage(new ProxyMessage.UdpBaseInfo()
                .setVisitorId(visitorId)
                .setServerPort(localAddress.getPort())
                .setTargetIp(targetIp)
                .setTargetPort(targetPort)));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("UDP visitor channel error", cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }
}
