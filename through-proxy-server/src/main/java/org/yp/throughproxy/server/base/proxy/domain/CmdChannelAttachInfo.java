package org.yp.throughproxy.server.base.proxy.domain;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

/**
 * @program: through-proxy
 * @description:
 * @author: yp
 * @create: 2024-08-31 14:12
 **/
@Accessors(chain = true)
@Data
public class CmdChannelAttachInfo {

    /**
     * 用户通道映射
     */
    private Map<String, Channel> visitorChannelMap;

    /**
     * 服务端端口集合
     */
    private Set<Integer> serverPorts;

    private Integer licenseId;

    private String ip;

}
