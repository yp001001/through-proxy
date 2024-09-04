package org.yp.throughproxy.client.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.yp.throughproxy.client.core.*;
import org.yp.throughproxy.core.*;
import org.yp.throughproxy.core.dispatcher.DefaultDispatcher;
import org.yp.throughproxy.core.dispatcher.Dispatcher;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @program: through-proxy
 * @description: 代理配置
 * @author: yp
 * @create: 2024-08-31 18:21
 **/
@Configuration
public class ProxyConfiguration implements LifecycleBean {
    @Override
    public void start() throws Throwable {
        List<ProxyMessageHandler> proxyMessageHandlerList = Solon.context().getBeansOfType(ProxyMessageHandler.class);
        Dispatcher<ProxyMessageHandler, ProxyMessage> dispatcher = new DefaultDispatcher<>("MessageDispatcher", proxyMessage ->
                ProxyDataTypeEnum.of((int) proxyMessage.getType()) == null ? null : ProxyDataTypeEnum.of((int) proxyMessage.getType()).getName());
        Solon.context().wrapAndPut(Dispatcher.class, dispatcher);
    }


    @Bean("tunnelWorkerGroup")
    public NioEventLoopGroup tunnelWorkerGroup(@Inject ProxyConfig proxyConfig) {
        return new NioEventLoopGroup(proxyConfig.getTunnel().getThreadCount());
    }

    @Bean("cmdTunnelBootstrap")
    public Bootstrap cmdTunnelBootstrap(@Inject ProxyConfig proxyConfig,
                                        @Inject("tunnelWorkerGroup") NioEventLoopGroup tunnelWorkerGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(tunnelWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        if (proxyConfig.getTunnel().getTransferLogEnable() != null && proxyConfig.getTunnel().getTransferLogEnable()) {
                            channel.pipeline().addFirst(new LoggingHandler(CmdChannelHandler.class));
                        }
                        channel.pipeline().addLast(new ProxyMessageDecoder(proxyConfig.getProtocol().getMaxFrameLength(),
                                proxyConfig.getProtocol().getLengthFieldOffset(), proxyConfig.getProtocol().getLengthFieldLength(),
                                proxyConfig.getProtocol().getLengthAdjustment(), proxyConfig.getProtocol().getInitialBytesToStrip()));
                        channel.pipeline().addLast(new ProxyMessageEncoder());
                        channel.pipeline().addLast(new IdleStateHandler(proxyConfig.getProtocol().getReadIdleTime(), proxyConfig.getProtocol().getWriteIdleTime(), proxyConfig.getProtocol().getAllIdleTimeSeconds()));
                        channel.pipeline().addLast(new CmdChannelHandler());
                    }
                });

        bootstrap.remoteAddress(new InetSocketAddress(proxyConfig.getTunnel().getServerIp(), proxyConfig.getTunnel().getServerPort()));
        return bootstrap;
    }

    @Bean("tcpRealServerWorkGroup")
    public NioEventLoopGroup tcpRealServerWorkGroup() {
        return new NioEventLoopGroup();
    }

    @Bean("realServerBootstrap")
    public Bootstrap realServerBootstrap(@Inject ProxyConfig proxyConfig,
                                         @Inject("tcpRealServerWorkGroup") NioEventLoopGroup tcpRealServerWorkGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(tcpRealServerWorkGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (null != proxyConfig.getTunnel().getTransferLogEnable()
                                && proxyConfig.getTunnel().getTransferLogEnable()) {
                            ch.pipeline().addFirst(new LoggingHandler(RealServerChannelHandler.class));
                        }
                        ch.pipeline().addLast(new RealServerChannelHandler());
                    }
                });
        return bootstrap;
    }

    @Bean("tcpProxyTunnelBootstrap")
    public Bootstrap tcpProxyTunnelBootstrap(@Inject ProxyConfig proxyConfig,
                                             @Inject("tunnelWorkerGroup") NioEventLoopGroup tunnelWorkerGroup){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(tunnelWorkerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.remoteAddress(InetSocketAddress.createUnresolved(proxyConfig.getTunnel().getServerIp(), proxyConfig.getTunnel().getServerPort()));
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                if (proxyConfig.getTunnel().getSslEnable()) {
                    // ch.pipeline().addLast(ProxyUtil.createSslHandler(proxyConfig));
                }
                if (null != proxyConfig.getTunnel().getTransferLogEnable() && proxyConfig.getTunnel().getTransferLogEnable()) {
                    ch.pipeline().addFirst(new LoggingHandler(TcpProxyChannelHandler.class));
                }
                ch.pipeline().addLast(new ProxyMessageDecoder(proxyConfig.getProtocol().getMaxFrameLength(),
                        proxyConfig.getProtocol().getLengthFieldOffset(), proxyConfig.getProtocol().getLengthFieldLength(),
                        proxyConfig.getProtocol().getLengthAdjustment(), proxyConfig.getProtocol().getInitialBytesToStrip()));
                ch.pipeline().addLast(new ProxyMessageEncoder());
                ch.pipeline().addLast(new IdleStateHandler(proxyConfig.getProtocol().getReadIdleTime(), proxyConfig.getProtocol().getWriteIdleTime(), proxyConfig.getProtocol().getAllIdleTimeSeconds()));
                ch.pipeline().addLast(new TcpProxyChannelHandler());
            }
        });
        return bootstrap;
    }

    @Bean("udpProxyTunnelBootstrap")
    public Bootstrap udpProxyTunnelBootstrap(@Inject ProxyConfig proxyConfig,
                                             @Inject("tunnelWorkerGroup") NioEventLoopGroup tunnelWorkerGroup){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(tunnelWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (null != proxyConfig.getTunnel().getTransferLogEnable() && proxyConfig.getTunnel().getTransferLogEnable()) {
                            ch.pipeline().addFirst(new LoggingHandler(UdpProxyChannelHandler.class));
                        }
                        ch.pipeline().addLast(new ProxyMessageDecoder(proxyConfig.getProtocol().getMaxFrameLength(),
                                proxyConfig.getProtocol().getLengthFieldOffset(), proxyConfig.getProtocol().getLengthFieldLength(),
                                proxyConfig.getProtocol().getLengthAdjustment(), proxyConfig.getProtocol().getInitialBytesToStrip()));
                        ch.pipeline().addLast(new ProxyMessageEncoder());
                        ch.pipeline().addLast(new IdleStateHandler(proxyConfig.getProtocol().getReadIdleTime(), proxyConfig.getProtocol().getWriteIdleTime(), proxyConfig.getProtocol().getAllIdleTimeSeconds()));
                        ch.pipeline().addLast(new UdpProxyChannelHandler());
                    }
                });
        bootstrap.remoteAddress(proxyConfig.getTunnel().getServerIp(), proxyConfig.getTunnel().getServerPort());
        return bootstrap;
    }

    @Bean("udpWorkGroup")
    public NioEventLoopGroup udpWorkGroup(@Inject ProxyConfig proxyConfig) {
        // 暂时先公用此配置
        return new NioEventLoopGroup(proxyConfig.getClient().getUdp().getWorkThreadCount());
    }

    @Bean("udpServerBootstrap")
    public Bootstrap udpServerBootstrap(@Inject ProxyConfig proxyConfig,
                                         @Inject("udpWorkGroup") NioEventLoopGroup udpWorkGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(udpWorkGroup)
                .channel(NioDatagramChannel.class)
                // 广播
                .option(ChannelOption.SO_BROADCAST, true)
                // 设置读缓冲区为2M
                .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
                // 设置写缓冲区为1M
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (null != proxyConfig.getClient().getUdp().getTransferLogEnable() && proxyConfig.getClient().getUdp().getTransferLogEnable()) {
                            ch.pipeline().addFirst(new LoggingHandler(UdpRealServerHandler.class));
                        }
                        pipeline.addLast(udpWorkGroup, new UdpRealServerHandler());
                    }
                });

        return bootstrap;
    }

}
