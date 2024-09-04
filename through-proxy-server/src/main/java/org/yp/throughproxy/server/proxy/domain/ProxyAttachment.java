package org.yp.throughproxy.server.proxy.domain;

import io.netty.channel.Channel;

import java.util.function.BiConsumer;

/**
 * @author: yp
 * @date: 2024/9/4 10:44
 * @description:代理连接附件
 */
public class ProxyAttachment {

    private Channel channel;

    private byte[] bytes;

    private BiConsumer<Channel, byte[]> executor;

    public ProxyAttachment(Channel channel, byte[] bytes, BiConsumer<Channel, byte[]> executor) {
        this.channel = channel;
        this.bytes = bytes;
        this.executor = executor;
    }

    public void execute() {
        if (null != executor) {
            this.executor.accept(channel, bytes);
        }
    }

}
