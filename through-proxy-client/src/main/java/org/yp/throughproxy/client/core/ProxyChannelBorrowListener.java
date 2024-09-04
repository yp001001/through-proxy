package org.yp.throughproxy.client.core;

import io.netty.channel.Channel;

/**
 * @author: yp
 * @date: 2024/9/3 15:48
 * @description:
 */
public interface ProxyChannelBorrowListener {

    void success(Channel channel);

    void error(Throwable cause);

}
