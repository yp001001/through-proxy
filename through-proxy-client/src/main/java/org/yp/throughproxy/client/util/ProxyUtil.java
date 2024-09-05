package org.yp.throughproxy.client.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.Solon;
import org.yp.throughproxy.client.config.ProxyConfig;
import org.yp.throughproxy.client.core.ProxyChannelBorrowListener;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.util.FileUtil;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @program: through-proxy
 * @description:
 * @author: yp
 * @create: 2024-09-01 14:27
 **/
@Slf4j
public class ProxyUtil {

    private static String clientId;

    private static volatile Channel cmdChannel;

    private static final String CLIENT_ID_FILE = ".NEUTRINO_PROXY_CLIENT_ID";

    private static ConcurrentLinkedQueue<Channel> tcpProxyChannelPool = new ConcurrentLinkedQueue<Channel>();
    private static ConcurrentLinkedQueue<Channel> udpProxyChannelPool = new ConcurrentLinkedQueue<>();

    private static Map<String, Channel> realServerChannels = new ConcurrentHashMap<>();

    private static final int MAX_POOL_SIZE = 100;

    public static String getClientId() {
        if (StringUtils.isNotBlank(clientId)) {
            return clientId;
        }
        ProxyConfig proxyConfig = Solon.context().getBean(ProxyConfig.class);
        if (StringUtils.isNotBlank(proxyConfig.getTunnel().getClientId())) {
            clientId = proxyConfig.getTunnel().getClientId();
            return clientId;
        }
        String id = FileUtil.readContentAsString(CLIENT_ID_FILE);
        if (StringUtils.isNotBlank(id)) {
            clientId = id;
            return id;
        }
        id = UUID.randomUUID().toString().replace("-", "");
        FileUtil.write(CLIENT_ID_FILE, id);
        clientId = id;
        return id;
    }

    public static void setCmdChannel(Channel channel) {
        ProxyUtil.cmdChannel = channel;
    }


    public static void borrowTcpProxyChannel(Bootstrap tcpProxyTunnelBootstrap, final ProxyChannelBorrowListener borrowListener) {
        Channel channel = tcpProxyChannelPool.poll();
        if (Objects.nonNull(channel)) {
            borrowListener.success(channel);
            return;
        }

        tcpProxyTunnelBootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                borrowListener.success(future.channel());
            } else {
                borrowListener.error(future.cause());
            }
        });
    }


    public static void borrowUdpProxyChanel(Bootstrap udpProxyTunnelBootstrap, ProxyChannelBorrowListener borrowListener) {
        Channel channel = udpProxyChannelPool.poll();
        if (null != channel) {
            borrowListener.success(channel);
            return;
        }

        udpProxyTunnelBootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                borrowListener.success(future.channel());
            } else {
                borrowListener.error(future.cause());
            }
        });
    }

    public static void addRealServerChannel(String visitorId, Channel realServerChannel) {
        realServerChannels.put(visitorId, realServerChannel);
    }

    public static void setRealServerChannelVisitor(Channel realServerChannel, String visitorId) {
        realServerChannel.attr(Constants.VISITOR_ID).set(visitorId);
    }

    public static String getVisitorIdByRealServerChannel(Channel channel) {
        if (null == channel || null == channel.attr(Constants.VISITOR_ID)) {
            return null;
        }

        return channel.attr(Constants.VISITOR_ID).get();
    }

    public static void returnTcpProxyChannel(Channel channel) {
        if (tcpProxyChannelPool.size() > MAX_POOL_SIZE) {
            channel.close();
        } else {
            // 复用与代理服务端的channel
            channel.config().setOption(ChannelOption.AUTO_READ, true);
            channel.attr(Constants.NEXT_CHANNEL).remove();
            tcpProxyChannelPool.offer(channel);
        }
    }

    public static void removeTcpProxyChannel(Channel channel) {
        tcpProxyChannelPool.remove(channel);
    }

    public static Channel removeRealServerChannel(String visitorId) {
        return realServerChannels.remove(visitorId);
    }

    public static void clearRealServerChannels() {
        for (String visitorId : realServerChannels.keySet()) {
            Channel realSeverChannel = realServerChannels.get(visitorId);
            if (null != realSeverChannel && realSeverChannel.isActive()) {
                realSeverChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
