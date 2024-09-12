package org.yp.throughproxy.server.proxy.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.ProxyMessage;
import org.yp.throughproxy.server.base.proxy.domain.VisitorChannelAttachInfo;
import org.yp.throughproxy.server.constant.NetworkProtocolEnum;
import org.yp.throughproxy.server.proxy.domain.ProxyAttachment;
import org.yp.throughproxy.server.util.ProxyUtil;

/**
 * @program: through-proxy
 * @description:
 * @author: yp
 * @create: 2024-09-12 20:59
 **/
@Slf4j
public class HttpVisitorChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex();
        ProxyAttachment proxyAttachment = new ProxyAttachment(ctx.channel(), bytes, (channel, buf) -> {
            Channel proxyChannel = channel.attr(Constants.NEXT_CHANNEL).get();
            if (null == proxyChannel) {
                // 该端口还没有代理客户端
                ctx.channel().close();
                return;
            }

            proxyChannel.writeAndFlush(ProxyMessage.buildTransferMessage(ProxyUtil.getVisitorIdByChannel(channel), bytes));

            // 增加流量计数
//            VisitorChannelAttachInfo visitorChannelAttachInfo = ProxyUtil.getAttachInfo(channel);
//            Solon.context().getBean(FlowReportService.class).addWriteByte(visitorChannelAttachInfo.getLicenseId(), bytes.length);
        });

        String visitorId = ProxyUtil.getVisitorIdByChannel(ctx.channel());
        if (StringUtils.isNotBlank(visitorId)) {
            proxyAttachment.execute();
            return;
        }

        // 用户连接到代理服务器时，设置用户连接不可读，等待代理后端服务器连接成功后再改变为可读状态
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);


        // 根据域名拿到绑定的映射对应的cmdChannel
        Integer serverPort = ctx.channel().attr(Constants.SERVER_PORT).get();
        Channel cmdChannel = ProxyUtil.getCmdChannelByServerPort(serverPort);
        if (null == cmdChannel) {
            ctx.channel().close();
            return;
        }
        String lanInfo = ProxyUtil.getClientLanInfoByServerPort(serverPort);
        if (StringUtils.isBlank(lanInfo)) {
            ctx.channel().close();
            return;
        }

        visitorId = ProxyUtil.newVisitorId();
        ProxyUtil.addVisitorChannelToCmdChannel(NetworkProtocolEnum.HTTP, cmdChannel, visitorId, ctx.channel(), serverPort);
        ProxyUtil.addProxyConnectAttachment(visitorId, proxyAttachment);
        cmdChannel.writeAndFlush(ProxyMessage.buildConnectMessage(visitorId).setData(lanInfo.getBytes()));
    }

}
