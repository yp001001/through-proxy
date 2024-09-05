package org.yp.throughproxy.server.util;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.yp.throughproxy.core.ChannelAttribute;
import org.yp.throughproxy.server.base.proxy.domain.CmdChannelAttachInfo;
import org.yp.throughproxy.server.base.proxy.domain.VisitorChannelAttachInfo;
import org.yp.throughproxy.server.constant.NetworkProtocolEnum;
import org.yp.throughproxy.server.proxy.domain.ProxyAttachment;
import org.yp.throughproxy.server.proxy.domain.ProxyMapping;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private static final Map<Integer, Set<Integer>> licenseToServerPortMap = new ConcurrentHashMap<>();

    /**
     * 服务端口 -> 指令通道映射
     */
    private static Map<Integer, Channel> serverPortToCmdChannelMap = new ConcurrentHashMap<>();

    /**
     * 代理信息映射  serverport - clientIp:clientPort
     */
    private static final Map<Integer, String> proxyInfoMap = new ConcurrentHashMap<>();

    /**
     * 访问者ID生成器
     */
    private static AtomicLong visitorIdProducer = new AtomicLong();

    /**
     * licenseId - 客户端Id映射
     */
    private static Map<Integer, String> licenseIdClientIdMap = new ConcurrentHashMap<>();

    /**
     * license -> 指令通道映射
     */
    private static Map<Integer, Channel> licenseToCmdChannelMap = new ConcurrentHashMap<>();

    /**
     * cmdChannelAttachInfo.getUserChannelMap() 读写锁
     */
    private static final ReadWriteLock userChannelMapLock = new ReentrantReadWriteLock();

    /**
     * 服务端口 -> 访问通道映射
	 */
    private static Map<Integer, Channel> serverPortToVisitorChannel = new ConcurrentHashMap<>();

    /**
     * udp 代理 - connect附加映射
     */
    private static Map<String, ProxyAttachment> proxyConnectAttachmentMap = new HashMap<>();

    /**
     * 获取服务端对应的被代理端channel
     *
     * @param serverPort
     * @return
     */
    public static Channel getCmdChannelByServerPort(Integer serverPort) {
        return serverPortToCmdChannelMap.get(serverPort);
    }

    /**
     * 根据服务端端口获取客户端代理信息
     *
     * @param serverPort 服务端端口
     * @return 客户端代理信息
     */
    public static String getClientLanInfoByServerPort(Integer serverPort) {
        return proxyInfoMap.get(serverPort);
    }


    /**
     * 为访问者连接产生ID
     *
     * @return
     */
    public static String newVisitorId() {
        return String.valueOf(visitorIdProducer.incrementAndGet());
    }


    /**
     * 外部连接与被代理客户端连接关系
     *
     * @param networkProtocol
     * @param cmdChannel
     * @param visitorId
     * @param visitorChannel
     * @param serverPort
     */
    public static void addVisitorChannelToCmdChannel(NetworkProtocolEnum networkProtocol, Channel cmdChannel, String visitorId, Channel visitorChannel, int serverPort) {

        CmdChannelAttachInfo cmdChannelAttachInfo = getAttachInfo(cmdChannel);

        InetSocketAddress address = (InetSocketAddress) visitorChannel.localAddress();
        String lanInfo = getClientLanInfoByServerPort(address.getPort());

        VisitorChannelAttachInfo visitorChannelAttachInfo = new VisitorChannelAttachInfo()
                .setVisitorId(visitorId)
                .setProtocol(networkProtocol)
                .setLicenseId(cmdChannelAttachInfo.getLicenseId())
                .setLanInfo(lanInfo)
                .setServerPort(serverPort);

        if(NetworkProtocolEnum.UDP != networkProtocol){
            visitorChannelAttachInfo.setIp(((InetSocketAddress)visitorChannel.remoteAddress()).getAddress().getHostAddress());
        }

        setAttachInfo(visitorChannel, visitorChannelAttachInfo);

        try {
            userChannelMapLock.writeLock().lock();
            cmdChannelAttachInfo.getVisitorChannelMap().put(visitorId, visitorChannel);
        }finally {
            userChannelMapLock.writeLock().unlock();
        }
        serverPortToVisitorChannel.put(serverPort, visitorChannel);
    }

    public static <T> T getAttachInfo(Channel channel) {
        if (null == channel || null == channel.attr(CHANNEL_ATTR_KEY).get()) {
            return null;
        }

        return channel.attr(CHANNEL_ATTR_KEY).get().get("attachInfo");
    }

    private static void setAttachInfo(Channel channel, Object obj) {
        if (null == channel) {
            return;
        }
        channel.attr(CHANNEL_ATTR_KEY).set(ChannelAttribute.create()
                .set("attachInfo", obj));
    }

    /**
     * 设置licenseId对应的clientId
     *
     * @param licenseId
     * @param clientId
     */
    public static void setLicenseIdToClientIdMap(Integer licenseId, String clientId) {
        licenseIdClientIdMap.put(licenseId, clientId);
    }

    /**
     * 根据licenseId获取clientId
     *
     * @param licenseId
     * @return
     */
    public static String getClientIdByLicenseId(Integer licenseId) {
        return licenseIdClientIdMap.get(licenseId);
    }

    public static Channel getCmdChannelByLicenseId(Integer licenseId) {
        return licenseToCmdChannelMap.get(licenseId);
    }

    /**
     * 初始化代理信息
     *
     * @param licenseId
     * @param proxyMappings
     */
    public static void initProxyInfo(int licenseId, List<ProxyMapping> proxyMappings) {
        licenseToServerPortMap.put(licenseId, new HashSet<>());
        addProxyInfo(licenseId, proxyMappings);
    }

    public static void addProxyInfo(Integer licenseId, List<ProxyMapping> proxyMappingList) {
        if (!CollectionUtil.isEmpty(proxyMappingList)) {
            for (ProxyMapping proxyMapping : proxyMappingList) {
                licenseToServerPortMap.get(licenseId).add(proxyMapping.getServerPort());
                proxyInfoMap.put(proxyMapping.getServerPort(), proxyMapping.getLanInfo());
            }
        }
    }

    /**
     * 添加指令通道相关缓存信息
     *
     * @param licenseId
     * @param cmdChannel
     * @param serverPorts
     */
    public static void addCmdChannel(int licenseId, Channel cmdChannel, Set<Integer> serverPorts) {
        if (!CollectionUtil.isEmpty(serverPorts)) {
            for (int port : serverPorts) {
                serverPortToCmdChannelMap.put(port, cmdChannel);
            }
        }

        CmdChannelAttachInfo cmdChannelAttachInfo = getAttachInfo(cmdChannel);
        if (Objects.isNull(cmdChannelAttachInfo)) {
            // 保存client端的相关信息
            cmdChannelAttachInfo = new CmdChannelAttachInfo()
                    .setIp(((InetSocketAddress) cmdChannel.remoteAddress()).getAddress().getHostAddress())
                    .setLicenseId(licenseId)
                    .setVisitorChannelMap(new HashMap<>(16))
                    .setServerPorts(Sets.newHashSet());
            setAttachInfo(cmdChannel, cmdChannelAttachInfo);
        }

        if (!CollectionUtil.isEmpty(serverPorts)) {
            cmdChannelAttachInfo.getServerPorts().addAll(serverPorts);
        }

        licenseToCmdChannelMap.put(licenseId, cmdChannel);
    }

    /**
     * 根据代理客户端连接与用户编号获取用户连接
     * @param cmdChannel
     * @param visitorId
     * @return
     */
    public static Channel getVisitorChannel(Channel cmdChannel, String visitorId) {
        if (null == cmdChannel || null == getAttachInfo(cmdChannel)) {
            return null;
        }
        return ((CmdChannelAttachInfo)getAttachInfo(cmdChannel)).getVisitorChannelMap().get(visitorId);
    }

    public static String getVisitorIdByChannel(Channel channel) {
        if(null == channel || null == getAttachInfo(channel)){
            return null;
        }
        return ((VisitorChannelAttachInfo)getAttachInfo(channel)).getVisitorId();
    }

    /**
     * 添加visitorId与代理附件的映射
     * @param visitorId
     * @param proxyAttachment
     */
    public static void addProxyConnectAttachment(String visitorId, ProxyAttachment proxyAttachment){
        proxyConnectAttachmentMap.put(visitorId, proxyAttachment);
    }

    public static ProxyAttachment getProxyConnectAttachment(String visitorId){
        return proxyConnectAttachmentMap.get(visitorId);
    }

    public static void removeProxyConnectAttachment(String visitorId) {
        proxyConnectAttachmentMap.remove(visitorId);
    }

    /**
     * 代理客户端中的channel关闭
     * @param cmdChannel
     * @param visitorId
     * @return
     */
    public static Channel removeVisitorChannelFromCmdChannel(Channel cmdChannel, String visitorId) {
        if (null == getAttachInfo(cmdChannel) || null == ((CmdChannelAttachInfo) getAttachInfo(cmdChannel)).getVisitorChannelMap().get(visitorId)) {
            return null;
        }

        try {
            userChannelMapLock.writeLock().lock();
            return ((CmdChannelAttachInfo)getAttachInfo(cmdChannel)).getVisitorChannelMap().remove(visitorId);
        }finally {
            userChannelMapLock.writeLock().unlock();
        }
    }

    public static void removeCmdChannel(Channel cmdChannel) {

        if(Objects.isNull(cmdChannel) || null == getAttachInfo(cmdChannel)) {
            return;
        }

        CmdChannelAttachInfo cmdChannelAttachInfo = getAttachInfo(cmdChannel);

        for (Integer serverPort : cmdChannelAttachInfo.getServerPorts()) {
            serverPortToCmdChannelMap.remove(serverPort);
        }

        Map<String, Channel> visitorChannelMap = cmdChannelAttachInfo.getVisitorChannelMap();
        for (String visitorId : visitorChannelMap.keySet()) {
            Channel visitorChannel = visitorChannelMap.get(visitorId);
            if(null != visitorChannel && visitorChannel.isActive()){
                visitorChannel.close();
            }
        }
        cmdChannelAttachInfo.getVisitorChannelMap().clear();
    }

    public static void removeClientIdByLicenseId(Integer licenseId) {
        licenseIdClientIdMap.remove(licenseId);
    }
}
