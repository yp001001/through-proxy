package org.dromara.throughproxy.server.base.proxy.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.ProxyMessageDecoder;
import org.dromara.throughproxy.core.ProxyMessageEncoder;
import org.dromara.throughproxy.server.base.proxy.ProxyConfig;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.core.runtime.NativeDetector;

import java.util.Objects;


/**
 * @program: through-proxy
 * @description: 代理隧道服务
 * @author: yp
 * @create: 2024-08-31 14:37
 **/

@Slf4j
@Component
public class ProxyTunnelServer implements EventListener<AppLoadEndEvent> {

    @Inject
    private ProxyConfig proxyConfig;

    @Inject("tunnelBossGroup")
    private NioEventLoopGroup tunnelBossGroup;

    @Inject("tunnelWorkerGroup")
    private NioEventLoopGroup tunnelWorkerGroup;


    @Override
    public void onEvent(AppLoadEndEvent appLoadEndEvent) throws Throwable {
        // aot阶段，不启动代理服务
        if (NativeDetector.isNotAotRuntime()) {
            startProxyServer();
            startProxyServerForSSL();
        }
    }

    private void startProxyServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        proxyServerCommonInitHandler(socketChannel);
                    }
                });

        try {
            serverBootstrap.bind(proxyConfig.getTunnel().getPort()).sync();
            log.info("proxy server started，port：{}", proxyConfig.getTunnel().getPort());
        } catch (InterruptedException e) {
            log.error("proxy server error", e);
            throw new RuntimeException(e);
        }
    }


    private void startProxyServerForSSL() {
    }

    private void proxyServerCommonInitHandler(SocketChannel socketChannel) {
        if (Objects.nonNull(proxyConfig.getTunnel().getTransferLogEnable()) && proxyConfig.getTunnel().getTransferLogEnable()) {
            socketChannel.pipeline().addFirst(new LoggingHandler(ProxyTunnelServer.class));
        }
        // 添加编解码处理器
        ProxyConfig.Protocol protocol = proxyConfig.getProtocol();
        socketChannel.pipeline().addLast(new ProxyMessageDecoder(protocol.getMaxFrameLength(), protocol.getLengthFieldOffset(),
                protocol.getLengthFieldLength(), protocol.getLengthAdjustment(), protocol.getInitialBytesToStrip()));
        socketChannel.pipeline().addLast(new ProxyMessageEncoder());
        // 添加心跳检测处理器
        socketChannel.pipeline().addLast(new IdleStateHandler(protocol.getReaderIdleTime(), protocol.getWriteIdleTime(), protocol.getAllIdleTime()));
        // 添加自定义消息处理器
        socketChannel.pipeline().addLast(new ProxyTunnelChannelHandler());
    }

}
