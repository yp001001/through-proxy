package org.dromara.throughproxy.server.base.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.dromara.throughproxy.core.ProxyDataTypeEnum;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.core.dispatcher.DefaultDispatcher;
import org.dromara.throughproxy.core.dispatcher.Dispatcher;
import org.dromara.throughproxy.server.proxy.core.BytesMetricsHandler;
import org.dromara.throughproxy.server.proxy.core.TcpVisitorChannelHandler;
import org.dromara.throughproxy.server.proxy.security.TcpVisitorSecurityChannelHandler;
import org.dromara.throughproxy.server.proxy.security.VisitorFlowLimiterChannelHandler;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;

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
     * 外部与服务端打交道
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
                            // channel.pipeline().addFirst(new LoggingHandler(TcpVisitorChannelHandler.class));
                        }
                        channel.pipeline().addFirst(new BytesMetricsHandler());
                        channel.pipeline().addLast(new TcpVisitorSecurityChannelHandler());
                        channel.pipeline().addLast("flowLimiter", new VisitorFlowLimiterChannelHandler());
                        channel.pipeline().addLast(new TcpVisitorChannelHandler());
                    }
                });
        return bootstrap;
    }


    @Override
    public void stop() throws Throwable {
        LifecycleBean.super.stop();
    }
}
