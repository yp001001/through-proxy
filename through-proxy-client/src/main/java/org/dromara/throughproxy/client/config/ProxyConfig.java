package org.dromara.throughproxy.client.config;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * @program: through-proxy
 * @description: 代理配置信息
 * @author: yp
 * @create: 2024-08-29 21:20
 **/
@Data
@Component
public class ProxyConfig {

    @Inject("${neutrino.proxy.protocol}")
    private Protocol protocol;
    @Inject("${neutrino.proxy.tunnel}")
    private Tunnel tunnel;
    @Inject("${neutrino.proxy.client}")
    private Client client;


    @Data
    public static class Protocol{
        // frame最大长度
        private Integer maxFrameLength;
        // 长度偏移量
        private Integer lengthFieldOffset;
        // 长度表示字节数
        private Integer lengthFieldLenth;
        private Integer initialBytesToStrip;
        // 长度表示与数据之间的间隔字段
        private Integer lengthAdjustment;
        // 读超时时间
        private Integer readIdleTime;
        // 写超时时间
        private Integer writeIdleTime;
        private Integer allIdleTimeSeconds;
    }


    @Data
    public static class Tunnel{
        // 隧道ssl证书配置
        private String keyStorePassword;
        private String jksPath;
        // 服务端IP
        private String serverIp;
        // 服务端端口
        private Integer serverPort;
        private Boolean sslEnable;
        private Integer obtainLicenseInterval;
        private String licenseKey;
        private Integer threadCount;
        private String clientId;
        private Boolean transferLogEnable;
        private Boolean heartbeatLogEnable;
        private Reconnection reconnection;
    }

    @Data
    public static class Client {
        private Udp udp;
    }

    @Data
    public static class Reconnection{
        private Integer intervalSeconds;
        private Boolean unlimited;
    }

    @Data
    public static class Tcp{

    }

    @Data
    public static class Udp{
        private Integer  bossThreadCount;
        private Integer workThreadCount;
        private String puppetPortRange;
        private Boolean transferLogEnable;
    }

}
