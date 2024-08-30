package org.dromara.throughproxy.server.proxy.security;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.server.bo.FlowLimitBO;
import org.dromara.throughproxy.server.service.PortMappingService;
import org.noear.solon.Solon;

import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/8/30 17:10
 * @description:访问者流量限制器
 */
@Slf4j
public class VisitorFlowLimiterChannelHandler extends ChannelInboundHandlerAdapter {

    private final PortMappingService portMappingService = Solon.context().getBean(PortMappingService.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Boolean flowLimiterFlag = ctx.channel().attr(Constants.FLOW_LIMITER_FLAG).get();

        if (Objects.isNull(flowLimiterFlag) || !flowLimiterFlag) {

            Integer serverPort = ctx.channel().attr(Constants.SERVER_PORT).get();

            Long upLimitRate = null;
            Long downLimitRate = null;

            // 先获取端口映射上的限速设置
            FlowLimitBO flowLimitBO = portMappingService.getFlowLimitByServerPort(serverPort);
            if (null != flowLimitBO) {
                upLimitRate = flowLimitBO.getUpLimitRate();
                downLimitRate = flowLimitBO.getDownLimitRate();
            }

            if (upLimitRate != null || downLimitRate != null) {
                // 设置限速条件
                ctx.pipeline().addAfter("flowLimiter", " trafficShaping",
                        new ChannelTrafficShapingHandler(downLimitRate == null ? 0 : downLimitRate, upLimitRate == null ? 0 : upLimitRate, 100, 600000));
            }

            // 每个连接第一次处理之后。无论是否限速，该连接后续都不在处理，避免频繁执行影响性能
            ctx.channel().attr(Constants.FLOW_LIMITER_FLAG).set(true);
        }

        // 继续传播
        ctx.fireChannelRead(msg);

    }
}
