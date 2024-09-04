package org.yp.throughproxy.client.util;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: yp
 * @date: 2024/9/4 15:48
 * @description:
 */
@Accessors(chain = true)
@Data
public class LockChannel {
    // 端口号
    private int port;
    // 通道
    private Channel channel;
    // 期望的响应次数
    private int proxyResponses;
    // 超时时间（毫秒）
    private long proxyTimeoutMs;
    // 被获取的时间
    private Date takeTime;
    // 已经响应的次数
    private int responseCount;
}
