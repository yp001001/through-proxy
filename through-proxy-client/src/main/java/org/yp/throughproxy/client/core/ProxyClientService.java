package org.yp.throughproxy.client.core;

import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.yp.throughproxy.client.config.ProxyConfig;
import org.yp.throughproxy.client.util.ProxyUtil;
import org.yp.throughproxy.client.util.UdpServerUtil;
import org.yp.throughproxy.core.ProxyMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: through-proxy
 * @description: 代理客户端服务
 * @author: yp
 * @create: 2024-08-31 23:53
 **/
@Slf4j
@Component
public class ProxyClientService {

    @Inject
    private ProxyConfig proxyConfig;

    @Inject("cmdTunnelBootstrap")
    private Bootstrap cmdTunnelBootstrap;

    @Inject("udpServerBootstrap")
    private Bootstrap udpServerBootstrap;

    private volatile Channel channel;

    /**
     * 重试次数
     */
    private volatile AtomicInteger reconnectCount = new AtomicInteger(0);

    /**
     * 重连定时操作线程
     */
    private static final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("ClientReconnect"));

    @Init
    public void init() {
        // 设置重连
        reconnectExecutor.scheduleWithFixedDelay(() -> reconnect(), 10, proxyConfig.getTunnel().getReconnection().getIntervalSeconds(), TimeUnit.SECONDS);

        try {
            this.start();
            // 初始化udp连接池
            UdpServerUtil.initCache(proxyConfig, udpServerBootstrap);
        } catch (Exception e) {
            // 启动连不上也做一下重连，因此先catch异常
            log.error("[CmdChannel] start error", e);
        }
    }

    public void start() {
        if (StrUtil.isEmpty(proxyConfig.getTunnel().getServerIp())) {
            log.error("not found server-ip config.");
            Solon.stop();
            return;
        }
        if (null == proxyConfig.getTunnel().getServerPort()) {
            log.error("not found server-port config.");
            Solon.stop();
            return;
        }
        // 判断是否开启了SSL
        if (null != proxyConfig.getTunnel().getSslEnable()
                && proxyConfig.getTunnel().getSslEnable() && StrUtil.isBlank(proxyConfig.getTunnel().getJksPath())) {
            log.error("enable ssl but config is error");
            return;
        }
        if (StrUtil.isBlank(proxyConfig.getTunnel().getLicenseKey())) {
            log.error("licenseKey is null");
            return;
        }
        if (null == channel || !channel.isActive()) {
            try {
                connectProxyServer();
            } catch (Exception e) {
                log.error("client start error", e);
            }
        } else {
            channel.writeAndFlush(ProxyMessage.buildAuthMessage(proxyConfig.getTunnel().getLicenseKey(), ProxyUtil.getClientId()));
        }
    }


    /**
     * 连接代理服务器
     */
    private void connectProxyServer() throws InterruptedException {
        cmdTunnelBootstrap.connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            channel = channelFuture.channel();
                            // 连接成功，向服务器发送客户端认证信息
                            ProxyUtil.setCmdChannel(channel);
                            log.info("客户端连接服务端成功... 开始发送Auth请求...");
                            channel.writeAndFlush(ProxyMessage.buildAuthMessage(proxyConfig.getTunnel().getLicenseKey(), ProxyUtil.getClientId()));
                            log.info("[CmdChannel] connect proxy server success. channelId:{}", channelFuture.channel().id().asLongText());
                            reconnectCount.set(0);
                        } else {
                            log.info("[CmdChannel] connect proxy server failed!");
                        }
                    }
                }).sync();
    }


    public void reconnect() {
        try {
            if (channel != null) {
                if (channel.isActive()) {
                    return;
                }
                channel.close();
            }

            log.info("[CmdChannel] client reconnect seq:{}", reconnectCount.incrementAndGet());
            connectProxyServer();
        } catch (Exception e) {
            log.error("[CmdChannel] reconnect error", e);
        }
    }

}
