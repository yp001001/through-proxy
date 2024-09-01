package org.dromara.throughproxy.server.base.proxy;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * @program: through-proxy
 * @description: 服务端代理配置
 * @author: yp
 * @create: 2024-08-29 23:19
 **/
@Data
@Component
public class ProxyConfig {

    /**
     * 传输协议相关配置
     */
    @Inject("${through.proxy.protocol}")
    private Protocol protocol;

    /**
     * 代理服务配置
     */
    @Inject("${through.proxy.server}")
    private Server server;

    /**
     * 代理隧道配置
     */
    @Inject("${through.proxy.tunnel}")
    private Tunnel tunnel;


    @Data
    public static class Protocol {
        // 最大帧长度
        private Integer maxFrameLength;
        // 长度域偏移量
        private Integer lengthFieldOffset;
        // 数据域长度
        private Integer lengthFieldLength;
        // 跳过的字节数，如果希望接受的header+body的数据，此值就是0，如果只想接受body数据，那么需要跳过header所占用的字节数
        private Integer initialBytesToStrip;
        // 数据长度修正
        private Integer lengthAdjustment;
        // 读空闲时间
        private Integer readIdleTime;
        // 写空闲时间
        private Integer writeIdleTime;
        // 规定时间既没有得到读事件，也没有得到写事件触发
        private Integer allIdleTimeSeconds;
    }


    @Data
    public static class Server {
        private Tcp tcp;
        private Udp udp;
    }

    @Data
    public static class Tunnel {
        private Integer bossThreadCount;
        private Integer workThreadCount;
        private Integer port;
        private Integer sslPort;
        private String keyStorePassword;
        private String keyManagerPassword;
        private String jksPath;
        private Boolean transferLogEnable;
        private Boolean heartbeatLogEnable;
    }

    @Data
    public static class Tcp {
        private Integer bossThreadCount;
        private Integer workThreadCount;
        private String domainName;
        private Integer httpProxyPort;
        private Integer httpsProxyPort;
        private String keyStorePassword;
        private String jksPath;
        private Boolean transferLogEnable;
    }

    @Data
    public static class Udp {
        private Integer bossThreadCount;
        private Integer workThreadCount;
        private Boolean transferLogEnable;
    }
}
