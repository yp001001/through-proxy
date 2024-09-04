package org.yp.throughproxy.client.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.core.runtime.NativeDetector;
import org.yp.throughproxy.client.config.ProxyConfig;
import org.yp.throughproxy.client.constant.Constants;
import org.yp.throughproxy.client.core.CustomThreadFactory;
import org.yp.throughproxy.core.ProxyMessage;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author: yp
 * @date: 2024/9/4 15:42
 * @description:
 */
@Slf4j
public class UdpServerUtil {

    private static final Boolean isSupportUdp = Boolean.FALSE;
    private static int udpServerPortMin = 0;
    private static int udpServerPortMax = 0;
    private static int nextUdpServerPort = 0;
    private static Bootstrap udpServerBootstrap;
    private static int defaultUdpServerPort;
    private static Channel defaultUdpServerChannel;
    private static Map<Integer, Channel> portToChannelMap = new ConcurrentHashMap<>();

    /**
     * udp空闲端口池
     */
    private static ConcurrentLinkedQueue<Integer> udpServerFreePortPool = new ConcurrentLinkedQueue<>();
    private static List<LockChannel> lockChannelList = new ArrayList<>();
    /**
     * lockChannel扫描器
     */
    private static final ScheduledExecutorService lockChannelScanner =
            Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("lockChannelScanner"));


    /**
     * 初始化UDP缓存
     * 1. 初始化一个基础UDP服务，用于不需要响应的UDP转发
     * 2. 维护一个UDP服务池，用于需要响应的UDP转发
     *
     * @param proxyConfig
     * @param udpServerBootstrap
     * @return
     */
    public static void initCache(ProxyConfig proxyConfig, Bootstrap udpServerBootstrap) {
        // aot 阶段，不初始化UDP服务
        if (NativeDetector.isAotRuntime()) {
            return;
        }
        if (null == proxyConfig.getClient().getUdp() || StringUtils.isEmpty(proxyConfig.getClient().getUdp().getPuppetPortRange())) {
            return;
        }
        ProxyConfig.Udp udpConfig = proxyConfig.getClient().getUdp();
        String[] tmp = udpConfig.getPuppetPortRange().split("-");
        if (null == tmp || tmp.length != 2) {
            log.error("client udp config error!");
            return;
        }
        try {
            udpServerPortMin = Integer.parseInt(tmp[0]);
            udpServerPortMax = Integer.parseInt(tmp[1]);
            if (udpServerPortMax <= udpServerPortMin) {
                // 至少得给1个udp端口
                throw new RuntimeException("client udp config error!");
            }
            nextUdpServerPort = udpServerPortMin;
            UdpServerUtil.udpServerBootstrap = udpServerBootstrap;
            log.info("udp proxy server port: {} ~ {}", udpServerPortMin, udpServerPortMax);
            // 初始化udp服务
            initUdpServer();
            // 初始化lockChannel扫描器
            lockChannelScanner.scheduleWithFixedDelay(UdpServerUtil::lockChannelScan, 5, 3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("client udp config error!", e);
        }
    }

    /**
     * 初始化udp服务
     */
    private static void initUdpServer() {
        defaultUdpServerPort = nextUdpServerPort();
        defaultUdpServerChannel = bindPort(defaultUdpServerPort);
        // 初始化默认最多额外开启5个udp服务，其它的需要时在启动
        for (int i = 0; i < 5; i++) {
            if(!hasNextUdpServerPort()){
                return;
            }
            int port = nextUdpServerPort();
            Channel channel = bindPort(port);
            portToChannelMap.put(port, channel);
            udpServerFreePortPool.offer(port);
        }
        while(hasNextUdpServerPort()){
            udpServerFreePortPool.offer(nextUdpServerPort());
        }
    }


    public static synchronized int nextUdpServerPort() {
        return nextUdpServerPort++;
    }

    public static Boolean hasNextUdpServerPort() {
        return nextUdpServerPort <= udpServerPortMax;
    }

    private static Channel bindPort(int port) {
        try {
            ChannelFuture future = udpServerBootstrap.bind(port).sync();
            log.info("[udp server] bind port:{} success!", port);
            return future.channel();
        } catch (InterruptedException e) {
            log.error("[udp server] bind port:{} error!", port);
            throw new RuntimeException(e);
        }
    }


    private static synchronized void lockChannelScan() {
        try{
            if(lockChannelList.isEmpty()){
                return;
            }
            Iterator<LockChannel> iter = lockChannelList.iterator();
            while(iter.hasNext()){
                LockChannel lockChannel = iter.next();
                if (lockChannel.getResponseCount() >= lockChannel.getProxyResponses() ||
                        System.currentTimeMillis() - lockChannel.getTakeTime().getTime() >= lockChannel.getProxyTimeoutMs()){
                    iter.remove();
                    UdpChannelBindInfo udpChannelBindInfo = lockChannel.getChannel().attr(Constants.UDP_CHANNEL_BIND_KEY).get();
                    // 此处必须释放代理隧道
//                    closeChannel(udpChannelBindInfo.getTunnelChannel());
//                    lockChannel.getChannel().attr(Constants.UDP_CHANNEL_BIND_KEY).set(null);
                    udpServerFreePortPool.offer(lockChannel.getPort());
                    log.debug("[udp channel]release udp channel port:{}", lockChannel.getPort());
                }
            }
        }catch (Exception e){
            log.error("lockChannelSacn error ", e);
        }
    }


    /**
     * 获取一个可用的udp通道
     * 1. 如果期待的响应为0，或者超时时间<=0，则认为不需要响应，直接返回默认的udp服务
     * 2. 从可用端口队列中找到一个可用端口，若不存在可用端口，则降级为不需要响应，返回默认的udp服务。否则继续下一步
     * 3、根据该端口找到udp服务通道，找不到则绑定端口开启一个通道并返回。将该端口添加到锁定列表
     * 4、维护一个定时器的，定时扫描锁定列表，及时释放锁定的端
     * @param info
     * @param tunnelChannel
     * @return
     */
    public static synchronized Channel takeChannel(ProxyMessage.UdpBaseInfo info, Channel tunnelChannel){
//        if(info.getProxyResponses() <= 0 || info.getProxyTimeoutMs() <= 0){
//            return defaultUdpServerChannel;
//        }
        Integer port = udpServerFreePortPool.poll();
        if(null == port){
            return defaultUdpServerChannel;
        }
        Channel channel = portToChannelMap.get(port);
        if(Objects.isNull(channel)){
            channel = bindPort(port);
            portToChannelMap.put(port, channel);
        }

        // 添加到锁定队列
        LockChannel lockChannel = new LockChannel()
                .setPort(port)
                .setChannel(channel)
                .setProxyResponses(info.getProxyResponses())
                .setResponseCount(0)
                .setTakeTime(new Date())
                .setProxyTimeoutMs(info.getProxyTimeoutMs());
        channel.attr(Constants.UDP_CHANNEL_BIND_KEY).set(new UdpChannelBindInfo()
                .setTunnelChannel(tunnelChannel)
                .setVisitorId(info.getVisitorId())
                .setVisitorIp(info.getVisitorIp())
                .setVisitorPort(info.getVisitorPort())
                .setServerPort(info.getServerPort())
                .setTargetIp(info.getTargetIp())
                .setTargetPort(info.getTargetPort())
                .setLockChannel(lockChannel));
        lockChannelList.add(lockChannel);
        return channel;
    }

    private static void closeChannel(Channel channel) {
        try {
            // 关闭通道
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            // ignore
        }
    }


}
