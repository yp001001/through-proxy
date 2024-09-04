package org.yp.throughproxy.client.util;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: yp
 * @date: 2024/9/4 16:14
 * @description:
 */
@Accessors(chain = true)
@Data
public class UdpChannelBindInfo {
    private Channel tunnelChannel;
    private LockChannel lockChannel;
    private String visitorId;
    private String visitorIp;
    private int visitorPort;
    private int serverPort;
    private String targetIp;
    private int targetPort;
}
