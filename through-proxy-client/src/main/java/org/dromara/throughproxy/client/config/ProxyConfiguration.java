package org.dromara.throughproxy.client.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.dromara.throughproxy.client.core.CmdChannelHandler;
import org.dromara.throughproxy.client.core.RealServerChannelHandler;
import org.dromara.throughproxy.client.core.TcpProxyChannelHandler;
import org.dromara.throughproxy.core.*;
import org.dromara.throughproxy.core.dispatcher.DefaultDispatcher;
import org.dromara.throughproxy.core.dispatcher.Dispatcher;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;

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

}
