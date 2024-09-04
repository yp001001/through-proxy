package org.yp.throughproxy.server.base.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.yp.throughproxy.core.ProxyDataTypeEnum;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.core.dispatcher.DefaultDispatcher;
import org.yp.throughproxy.core.dispatcher.Dispatcher;
import org.yp.throughproxy.server.proxy.core.BytesMetricsHandler;
import org.yp.throughproxy.server.proxy.core.TcpVisitorChannelHandler;
import org.yp.throughproxy.server.proxy.core.UdpVisitorChannelHandler;
import org.yp.throughproxy.server.proxy.security.TcpVisitorSecurityChannelHandler;
import org.yp.throughproxy.server.proxy.security.UdpVisitorSecurityChannelHandler;
import org.yp.throughproxy.server.proxy.security.VisitorFlowLimiterChannelHandler;

/**
 * @author: yp
 * @date: 2024/8/30 10:58
 * @description:代理配置
 */
@Configuration
public class ProxyConfiguration implements LifecycleBean {

    @Override
    public void start() throws Throwable {
        Dispatcher<ChannelHandlerContext, ProxyMessage> dispatcher = new DefaultDispatcher<>("MessageDispatcher",
                (proxyMessage -> ProxyDataTypeEnum.of((int) proxyMessage.getType()) == null ?
                        null : ProxyDataTypeEnum.of((int) proxyMessage.getType()).getName()));
        Solon.context().wrapAndPut(Dispatcher.class, dispatcher);
    }

    @Bean("tcpServerBossGroup")
    public NioEventLoopGroup tcpServerBossGroup(@Inject ProxyConfig proxyConfig) {
        return new NioEventLoopGroup(proxyConfig.getServer().getTcp().getBossThreadCount());
    }

    @Bean("tcpServerWorkerGroup")
    public NioEventLoopGroup tcpServerWorkerGroup(@Inject ProxyConfig proxyConfig) {
        return new NioEventLoopGroup(proxyConfig.getServer().getTcp().getWorkThreadCount());
    }

    /**
     * client端与server端打交道
     *
     * @param tcpServerBossGroup
     * @param tcpServerWorkerGroup
     * @param proxyConfig
     * @return
     */
    @Bean("tcpServerBootstrap")
    public ServerBootstrap tcpServerBootstrap(@Inject("tcpServerBossGroup") NioEventLoopGroup tcpServerBossGroup,
                                              @Inject("tcpServerWorkerGroup") NioEventLoopGroup tcpServerWorkerGroup,
                                              @Inject ProxyConfig proxyConfig) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(tcpServerBossGroup, tcpServerWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        if (null != proxyConfig.getServer().getTcp().getTransferLogEnable()
                                && proxyConfig.getServer().getTcp().getTransferLogEnable()) {
                             channel.pipeline().addFirst(new LoggingHandler(TcpVisitorChannelHandler.class));
                        }
                        channel.pipeline().addFirst(new BytesMetricsHandler());
                        channel.pipeline().addLast(new TcpVisitorSecurityChannelHandler());
                        channel.pipeline().addLast("flowLimiter", new VisitorFlowLimiterChannelHandler());
                        channel.pipeline().addLast(new TcpVisitorChannelHandler());
                    }
                });
        return bootstrap;
    }


    @Bean("udpServerBossGroup")
    public NioEventLoopGroup udpServerBossGroup(@Inject ProxyConfig proxyConfig){
        return new NioEventLoopGroup(proxyConfig.getServer().getUdp().getBossThreadCount());
    }

    @Bean("udpServerWorkerGroup")
    public NioEventLoopGroup udpServerWorkerGroup(@Inject ProxyConfig proxyConfig){
        return new NioEventLoopGroup(proxyConfig.getServer().getUdp().getWorkThreadCount());
    }

    @Bean("udpServerBootstrap")
    public Bootstrap udpServerBootstarp(@Inject("udpServerBossGroup") NioEventLoopGroup udpServerBossGroup,
                                        @Inject("udpServerWorkerGroup") NioEventLoopGroup udpServerWorkerGroup,
                                        @Inject ProxyConfig proxyConfig){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(udpServerBossGroup)
                .channel(NioDatagramChannel.class)
                // 设置广播
                .option(ChannelOption.SO_BROADCAST, true)
                // 读缓冲区
                .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
                // 写缓冲区
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        if(null != proxyConfig.getServer().getUdp().getTransferLogEnable() && proxyConfig.getServer().getUdp().getTransferLogEnable()){
                            ch.pipeline().addFirst(new LoggingHandler(UdpVisitorChannelHandler.class));
                        }
                        ch.pipeline().addLast(udpServerWorkerGroup, new UdpVisitorSecurityChannelHandler());
                        ch.pipeline().addLast("flowLimiter", new VisitorFlowLimiterChannelHandler());
                        ch.pipeline().addLast(udpServerWorkerGroup, new UdpVisitorChannelHandler());
                    }
                });
        return bootstrap;
    }


    @Bean("tunnelBossGroup")
    public NioEventLoopGroup tunnelBossGroup(@Inject ProxyConfig proxyConfig){
        return new NioEventLoopGroup(proxyConfig.getTunnel().getBossThreadCount());
    }

    @Bean("tunnelWorkerGroup")
    public NioEventLoopGroup tunnelWorkerGroup(@Inject ProxyConfig proxyConfig){
        return new NioEventLoopGroup(proxyConfig.getTunnel().getWorkThreadCount());
    }



    @Override
    public void stop() throws Throwable {
        LifecycleBean.super.stop();
    }
}
