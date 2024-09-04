package org.yp.throughproxy.client.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.yp.throughproxy.client.config.ProxyConfig;
import org.yp.throughproxy.core.*;
import org.yp.throughproxy.core.dispatcher.Match;

/**
 * @program: through-proxy
 * @description:
 * @author: yp
 * @create: 2024-09-01 14:57
 **/

@Slf4j
@Match(type = Constants.ProxyDataTypeName.AUTH)
@Component
public class AuthProxyMessageHandler implements ProxyMessageHandler {

    @Inject
    private ProxyConfig proxyConfig;

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {

        String info = proxyMessage.getInfo();
        ONode oNode = ONode.load(info);
        int code = oNode.get("code").getInt();
        log.info("Auth result:{}", info);
        if(ExceptionEnum.AUTH_FAILED.getCode().equals(code)){
            log.info("client auth failed, client stop");
            channelHandlerContext.channel().close();
            if(!proxyConfig.getTunnel().getReconnection().getUnlimited()){
                Solon.stop();
            }
        }

    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.AUTH.getName();
    }
}
