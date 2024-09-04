package org.yp.throughproxy.client.constant;

import io.netty.util.AttributeKey;
import org.yp.throughproxy.client.util.UdpChannelBindInfo;

/**
 * @author: yp
 * @date: 2024/9/4 16:15
 * @description:
 */
public interface Constants {
    AttributeKey<UdpChannelBindInfo> UDP_CHANNEL_BIND_KEY = AttributeKey.newInstance("udpChannelBindKey");

}
