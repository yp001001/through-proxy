package org.yp.throughproxy.server.proxy.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.yp.throughproxy.server.proxy.domain.MetricsCollector;

import java.net.InetSocketAddress;

/**
 * @author: yp
 * @date: 2024/8/30 11:42
 * @description:字节发送数据接收数据检测处理器
 */
public class BytesMetricsHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector metricsCollector = MetricsCollector.getCollector(socketAddress.getPort());
        metricsCollector.incrementReadMsgs(1);
        metricsCollector.incrementReadBytes(((ByteBuf) msg).readableBytes());
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector metricsCollector = MetricsCollector.getCollector(socketAddress.getPort());
        metricsCollector.incrementWriteMsgs(1);
        metricsCollector.incrementWriteBytes(((ByteBuf) msg).readableBytes());
        super.write(ctx, msg, promise);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector.getCollector(socketAddress.getPort()).getChannels().incrementAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector.getCollector(sa.getPort()).getChannels().decrementAndGet();
        super.channelInactive(ctx);
    }
}
