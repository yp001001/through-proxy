package org.dromara.throughproxy.server.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.dromara.throughproxy.core.Constants;
import org.dromara.throughproxy.core.ProxyDataTypeEnum;
import org.dromara.throughproxy.core.ProxyMessage;
import org.dromara.throughproxy.core.ProxyMessageHandler;
import org.dromara.throughproxy.core.dispatcher.Match;
import org.noear.solon.annotation.Component;

/**
 * @program: through-proxy
 * @description: 认证处理
 * @author: yp
 * @create: 2024-09-01 14:44
 **/

@Slf4j
@Match(type = Constants.ProxyDataTypeName.AUTH)
@Component
public class AuthProxyMessageHandler implements ProxyMessageHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
//        String info = proxyMessage.getInfo();
//        String[] splitInfo;
//        if(StrUtil.isBlank(info) || (splitInfo = info.split(",")).length != 2){
//            channelHandlerContext.writeAndFlush(ProxyMessage.buildErrMessage(ExceptionEnum.AUTH_ERROR));
//        }

        log.info("接受到auth请求 data：{}", JSON.toJSONString(proxyMessage));
        channelHandlerContext.writeAndFlush(ProxyMessage.buildAuthResultMessage(0, "success", ""));

    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.AUTH.getName();
    }

}
