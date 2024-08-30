package org.dromara.throughproxy.core;

import io.netty.util.AttributeKey;

import java.nio.channels.Channel;

/**
 * @author: yp
 * @date: 2024/8/30 11:03
 * @description:
 */
public interface Constants {

    AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");
    AttributeKey<Integer> SERVER_PORT = AttributeKey.newInstance("serverPort");
    AttributeKey<Boolean> FLOW_LIMITER_FLAG = AttributeKey.newInstance("flowLimiterFlag");

    interface ProxyDataTypeName {
        String HEARTBEAT = "HEARTBEAT";
        String AUTH = "AUTH";
        String CONNECT = "CONNECT";
        String DISCONNECT = "DISCONNECT";
        String TRANSFER = "TRANSFER";
        String UDP_CONNECT = "UDP_CONNECT";
        String UDP_DISCONNECT = "UDP_DISCONNECT";
        String UDP_TRANSFER = "UDP_TRANSFER";
        String ERROR = "ERROR";
        String PORT_MAPPING_SYNC = "PORT_MAPPING_SYNC";
    }

}
