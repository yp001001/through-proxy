package org.dromara.throughproxy.client.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.throughproxy.client.config.ProxyConfig;
import org.dromara.throughproxy.client.core.ProxyChannelBorrowListener;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.util.FileUtil;
import org.noear.solon.Solon;

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

    private static Map<String, Channel> realServerChannels = new ConcurrentHashMap<>();

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
}
