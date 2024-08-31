package org.dromara.throughproxy.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @program: through-proxy
 * @description: 消息处理解码器
 * @author: yp
 * @create: 2024-08-31 17:17
 **/
@Slf4j
public class ProxyMessageDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * @param maxFrameLength      最大帧长度
     * @param lengthFieldOffset   长度表示字节
     * @param lengthFieldLength   数据长度
     * @param lengthAdjustment    跳过几个字节
     * @param initialBytesToStrip 帧的开始部分需要忽略的字节数
     */
    public ProxyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        int bodyLength = byteBuf.readInt();

        ProxyMessage proxyMessage = new ProxyMessage();

        byte type = byteBuf.readByte();
        long serialNumber = byteBuf.readLong();

        int infoLength = byteBuf.readInt();

        String info = null;

        if (infoLength != 0) {
            byte[] infoBytes = new byte[infoLength];
            info = String.valueOf(byteBuf.readBytes(infoBytes));
        }

        byte[] data = null;

        if (bodyLength != Constants.BYTE_LENGTH + Constants.INFO_LENGTH + Constants.SERIALNUMBER_LENGTH) {
            int dataLength = bodyLength - Constants.BYTE_LENGTH - Constants.INFO_LENGTH - Constants.SERIALNUMBER_LENGTH;
            data = new byte[dataLength];
            byteBuf.readBytes(data);
        }

        proxyMessage.setType(type)
                .setInfo(info)
                .setData(data)
                .setSerialNumber(serialNumber);

        return proxyMessage;

    }
}
