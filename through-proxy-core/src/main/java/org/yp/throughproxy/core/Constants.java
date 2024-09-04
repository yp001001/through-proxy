package org.yp.throughproxy.core;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;


/**
 * @author: yp
 * @date: 2024/8/30 11:03
 * @description:
 */
public interface Constants {

    AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");
    AttributeKey<Integer> SERVER_PORT = AttributeKey.newInstance("serverPort");
    AttributeKey<Boolean> FLOW_LIMITER_FLAG = AttributeKey.newInstance("flowLimiterFlag");

    AttributeKey<String> VISITOR_ID = AttributeKey.newInstance("visitor_id");
    AttributeKey<Integer> LICENSE_ID = AttributeKey.newInstance("license_id");

    AttributeKey<String> TARGET_IP = AttributeKey.newInstance("targetIp");
    AttributeKey<Integer> TARGET_PORT = AttributeKey.newInstance("targetPort");
    AttributeKey<Integer> PROXY_RESPONSES = AttributeKey.newInstance("proxyResponses");
    AttributeKey<Long> PROXY_TIMEOUT_MS = AttributeKey.newInstance("proxyTimeoutMs");

    AttributeKey<InetSocketAddress> SENDER = AttributeKey.newInstance("sender");


    int BYTE_LENGTH = 1;
    int SERIALNUMBER_LENGTH = 8;

    int INFO_LENGTH = 4;

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
