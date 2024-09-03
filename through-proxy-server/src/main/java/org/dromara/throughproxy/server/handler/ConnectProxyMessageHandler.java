package org.dromara.throughproxy.server.handler;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.*;
import org.dromara.throughproxy.core.dispatcher.Match;
import org.dromara.throughproxy.server.constant.EnableStatusEnum;
import org.dromara.throughproxy.server.dal.entity.License;
import org.dromara.throughproxy.server.dal.entity.User;
import org.dromara.throughproxy.server.service.LicenseService;
import org.dromara.throughproxy.server.service.UserService;
import org.dromara.throughproxy.server.util.ProxyUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/9/3 16:49
 * @description:
 */
@Slf4j
@Match(type = Constants.ProxyDataTypeName.CONNECT)
@Component
public class ConnectProxyMessageHandler implements ProxyMessageHandler {

    @Inject
    private LicenseService licenseService;
    @Inject
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        String info = proxyMessage.getInfo();
        if(StrUtil.isBlank(info)){
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "info cannot be empty!"));
            log.error("connect info cannot be empty!");
            ctx.channel().close();
            return;
        }

        String[] infoArray = info.split("@");
        if(infoArray.length != 2){
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "info is invalid"));
            log.error("connect info is not valid!");
            ctx.channel().close();
            return;
        }

        // 校验携带的license是否合法
        String visitorId = infoArray[0];
        String licenseKey = infoArray[1];

        License license = licenseService.queryByKey(licenseKey);
        if(Objects.isNull(license)){
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "the license not found!"));
            ctx.channel().close();
            return;
        }

        if(EnableStatusEnum.DISABLE.getStatus().equals(license.getEnable())){
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "the license invalid!"));
            ctx.channel().close();
            return;
        }

        User user = userService.queryById(license.getUserId());
        if (null == user || EnableStatusEnum.DISABLE.getStatus().equals(user.getEnable())) {
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "the license invalid!"));
            ctx.channel().close();
            return;
        }

        Channel cmdChannel = ProxyUtil.getCmdChannelByLicenseId(license.getId());

        if (null == cmdChannel) {
            ctx.channel().writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.CONNECT_FAILED, "server error，cmd channel not found!"));
            ctx.channel().close();
            return;
        }

        Channel visitorChannel = ProxyUtil.getVisitorChannel(cmdChannel, visitorId);
        if(null == visitorChannel){
            return;
        }

        ctx.channel().attr(Constants.VISITOR_ID).set(visitorId);
        ctx.channel().attr(Constants.LICENSE_ID).set(license.getId());
        ctx.channel().attr(Constants.NEXT_CHANNEL).set(visitorChannel);
        visitorChannel.attr(Constants.NEXT_CHANNEL).set(ctx.channel());
        visitorChannel.attr(Constants.LICENSE_ID).set(license.getId());
        // 代理客户端与后端服务器连接成功，修改用户连接为可读状态
        visitorChannel.config().setOption(ChannelOption.AUTO_READ, true);

    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.CONNECT.getDesc();
    }
}
