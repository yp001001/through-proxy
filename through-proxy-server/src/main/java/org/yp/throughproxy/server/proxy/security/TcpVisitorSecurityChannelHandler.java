package org.yp.throughproxy.server.proxy.security;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.Constants;
import org.yp.throughproxy.core.util.IpUtil;
import org.yp.throughproxy.server.service.PortMappingService;
import org.yp.throughproxy.server.service.SecurityGroupService;

import java.net.InetSocketAddress;

/**
 * @author: yp
 * @date: 2024/8/30 14:16
 * @description:
 */
@Slf4j
public class TcpVisitorSecurityChannelHandler extends ChannelInboundHandlerAdapter {

    private final SecurityGroupService securityGroupService = Solon.context().getBean(SecurityGroupService.class);
    private final PortMappingService portMappingService = Solon.context().getBean(PortMappingService.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        Channel visitorChannel = ctx.channel();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) visitorChannel.localAddress();

        // 校验IP是否在该端口绑定给的安全组允许的规则内
        if(!securityGroupService.judgeAllow(IpUtil.getRemoteIp(ctx), portMappingService.getSecurityGroupIdByMappingPort(inetSocketAddress.getPort()))){
            ctx.channel().close();
            return;
        }

        ctx.fireChannelActive();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel visitorChannel = ctx.channel();

        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        // 判断IP是否在该端口绑定的安全组允许的规则内
        String ip = IpUtil.getRealRemoteIp(new String(bytes));
        if (StringUtils.isEmpty(ip)) {
            ip = IpUtil.getRemoteIp(ctx);
        }
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        if (!securityGroupService.judgeAllow(ip, portMappingService.getSecurityGroupIdByMappingPort(sa.getPort()))) {
            // 不在安全组规则放行范围内
            ctx.channel().close();
            return;
        }

        // 继续传播
        ctx.channel().attr(Constants.SERVER_PORT).set(sa.getPort());
        buf.resetReaderIndex();
        ctx.fireChannelRead(buf);
    }
}
