package org.dromara.throughproxy.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: through-proxy
 * @description: 消息处理编码器
 * @author: yp
 * @create: 2024-08-31 17:18
 **/
@Slf4j
public class ProxyMessageEncoder extends MessageToByteEncoder<ProxyMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage, ByteBuf byteBuf) throws Exception {
        int bodyLength = Constants.BYTE_LENGTH + Constants.SERIALNUMBER_LENGTH + Constants.INFO_LENGTH;

        if (proxyMessage.getInfo() != null) {
            bodyLength += proxyMessage.getInfo().length();
        }

        if (proxyMessage.getData() != null) {
            bodyLength += proxyMessage.getData().length;
        }

        byteBuf.writeInt(bodyLength);
        byteBuf.writeByte(proxyMessage.getType());
        byteBuf.writeLong(proxyMessage.getSerialNumber());

        if (proxyMessage.getInfo() != null) {
            byteBuf.writeInt(proxyMessage.getInfo().length());
            byteBuf.writeBytes(proxyMessage.getInfo().getBytes());
        } else {
            byteBuf.writeInt(0x00);
        }


        if (proxyMessage.getData() != null) {
            byteBuf.writeBytes(proxyMessage.getData());
        }


    }
}
