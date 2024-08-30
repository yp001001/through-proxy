package org.dromara.throughproxy.server.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.dromara.throughproxy.core.ChannelAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: yp
 * @date: 2024/8/30 18:13
 * @description:
 */
public class ProxyUtil {

    public static final AttributeKey<ChannelAttribute> CHANNEL_ATTR_KEY = AttributeKey.valueOf("netty.channel.attr");

    /**
     * license -> 服务端口映射
     */
    private static final Map<Integer, Set<Integer>> licenseToServerPortMap = new HashMap<>();

    /**
     * 服务端口 -> 指令通道映射
     */
    private static Map<Integer, Channel> serverPortToCmdChannelMap = new ConcurrentHashMap<>();

    /**
     * 代理信息映射
     */
    private static final Map<Integer, String> proxyInfoMap = new ConcurrentHashMap<>();

    /**
     * 访问者ID生成器
     */
    private static AtomicLong visitorIdProducer = new AtomicLong();

    /**
     * 获取服务端对应的被代理端channel
     * @param serverPort
     * @return
     */
    public static Channel getCmdChannelByServerPort(Integer serverPort){
        return serverPortToCmdChannelMap.get(serverPort);
    }

    /**
     * 根据服务端端口获取客户端代理信息
     * @param serverPort 服务端端口
     * @return 客户端代理信息
     */
    public static String getClientLanInfoByServerPort(Integer serverPort) {
        return proxyInfoMap.get(serverPort);
    }


    /**
     * 为访问者连接产生ID
     * @return
     */
    public static String newVisitorId() {
        return String.valueOf(visitorIdProducer.incrementAndGet());
    }


}