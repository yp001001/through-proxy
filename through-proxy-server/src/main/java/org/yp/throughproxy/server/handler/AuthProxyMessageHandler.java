package org.yp.throughproxy.server.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.yp.throughproxy.core.*;
import org.yp.throughproxy.core.dispatcher.Match;
import org.yp.throughproxy.server.constant.ClientConnectTypeEnum;
import org.yp.throughproxy.server.constant.EnableStatusEnum;
import org.yp.throughproxy.server.constant.OnlineStatusEnum;
import org.yp.throughproxy.server.constant.SuccessCodeEnum;
import org.yp.throughproxy.server.dal.LicenseMapper;
import org.yp.throughproxy.server.dal.UserMapper;
import org.yp.throughproxy.server.dal.entity.ClientConnectRecord;
import org.yp.throughproxy.server.dal.entity.License;
import org.yp.throughproxy.server.dal.entity.User;
import org.yp.throughproxy.server.service.ClientConnectRecordService;
import org.yp.throughproxy.server.service.VisitorChannelService;
import org.yp.throughproxy.server.util.ProxyUtil;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Objects;

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

    @Inject
    private LicenseMapper licenseMapper;
    @Inject
    private UserMapper userMapper;
    @Inject
    private VisitorChannelService visitorChannelService;
    @Inject
    private ClientConnectRecordService clientConnectRecordService;

    /**
     * 代理客户端连接代理服务端之后，处理代理客户端发送的Auth请求
     * 初始化对应的licenseKey的端口，将cmdChannel映射licenseId
     * @param ctx
     * @param proxyMessage
     */
    @Override
    public void handle(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        String ip = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        Date now = new Date();

        log.info("接受到auth请求 data：{}", JSON.toJSONString(proxyMessage));

        String info = proxyMessage.getInfo();
        String[] splitInfo;
        if (StrUtil.isBlank(info) || (splitInfo = info.split(",")).length != 2) {
            ctx.channel().writeAndFlush(ProxyMessage.buildAuthResultMessage(ExceptionEnum.AUTH_FAILED.getCode(), "info error!", info));
            ctx.channel().close();
            clientConnectRecordService.add(new ClientConnectRecord()
                    .setIp(ip)
                    .setType(ClientConnectTypeEnum.CONNECT.getType())
                    .setMsg(info)
                    .setCode(SuccessCodeEnum.FAIL.getCode())
                    .setErr("license cannot empty!")
                    .setCreateTime(now));
            return;
        }

        String licenseKey = splitInfo[0];
        String clientId = splitInfo[1];

        // 查询数据库是否存在对应的licenseId
        License license = licenseMapper.queryByLicenseKey(licenseKey);
        if (null == license) {
            log.warn("[client connection] license notfound info:{} ", info);
            ctx.channel().writeAndFlush(ProxyMessage.buildAuthResultMessage(ExceptionEnum.AUTH_FAILED.getCode(), "license not found!", licenseKey));
            ctx.channel().close();
            clientConnectRecordService.add(new ClientConnectRecord()
                    .setIp(ip)
                    .setType(ClientConnectTypeEnum.CONNECT.getType())
                    .setMsg(licenseKey)
                    .setCode(SuccessCodeEnum.FAIL.getCode())
                    .setErr("license notfound!")
                    .setCreateTime(now)
            );
            return;
        }

        if (EnableStatusEnum.DISABLE.getStatus().equals(license.getEnable())) {
            log.warn("[client connection] the license disabled info:{} ", info);
            ctx.channel().writeAndFlush(ProxyMessage.buildAuthResultMessage(ExceptionEnum.AUTH_FAILED.getCode(), "the license disabled!", licenseKey));
            ctx.channel().close();
            clientConnectRecordService.add(new ClientConnectRecord()
                    .setIp(ip)
                    .setLicenseId(license.getId())
                    .setType(ClientConnectTypeEnum.CONNECT.getType())
                    .setMsg(licenseKey)
                    .setCode(SuccessCodeEnum.FAIL.getCode())
                    .setErr("the license disabled!")
                    .setCreateTime(now));
            return;
        }

        // 查询数据库是否有对应的用户，且用户状态开启
        User user = userMapper.selectById(license.getUserId());
        if (null == user || EnableStatusEnum.DISABLE.getStatus().equals(user.getEnable())) {
            log.warn("[client connection] the license invalid info:{} ", info);
            ctx.channel().writeAndFlush(ProxyMessage.buildAuthResultMessage(ExceptionEnum.AUTH_FAILED.getCode(), "the license invalid!", licenseKey));
            ctx.channel().close();
            clientConnectRecordService.add(new ClientConnectRecord()
                    .setIp(ip)
                    .setLicenseId(license.getId())
                    .setType(ClientConnectTypeEnum.CONNECT.getType())
                    .setMsg(licenseKey)
                    .setCode(SuccessCodeEnum.FAIL.getCode())
                    .setErr("the license invalid!")
                    .setCreateTime(now));
            return;
        }


        Channel cmdChannel = ProxyUtil.getCmdChannelByLicenseId(license.getId());
        if(Objects.nonNull(cmdChannel)){
            String _clientId = ProxyUtil.getClientIdByLicenseId(license.getId());
            if(!clientId.equals(_clientId)){
                log.warn("[client connection] the license on another no used info:{} _clientId:{}", info, _clientId);
                ctx.channel().writeAndFlush(ProxyMessage.buildAuthResultMessage(ExceptionEnum.AUTH_FAILED.getCode(), "the license on another no used!", licenseKey));
                ctx.channel().close();
                clientConnectRecordService.add(new ClientConnectRecord()
                        .setIp(ip)
                        .setLicenseId(license.getId())
                        .setType(ClientConnectTypeEnum.CONNECT.getType())
                        .setMsg(licenseKey)
                        .setCode(SuccessCodeEnum.FAIL.getCode())
                        .setErr("the license on another no used!")
                        .setCreateTime(now));
                return;
            }
        }

        ctx.channel().writeAndFlush(ProxyMessage.buildAuthResultMessage(ExceptionEnum.SUCCESS.getCode(), "auth success!", licenseKey));

        clientConnectRecordService.add(new ClientConnectRecord()
                .setIp(ip)
                .setLicenseId(license.getId())
                .setType(ClientConnectTypeEnum.CONNECT.getType())
                .setMsg(info)
                .setCode(SuccessCodeEnum.SUCCESS.getCode())
                .setCreateTime(now));

        // 设置当前licenseId对应的客户端ID
        ProxyUtil.setLicenseIdToClientIdMap(license.getId(), clientId);

        log.warn("[client connection] auth success info:{} ", info);

        // 更新license状态
        licenseMapper.updateOnlineStatus(license.getId(), OnlineStatusEnum.ONLINE.getStatus(), now);

        // 初始化visitorChannel
        visitorChannelService.initVisitorChannel(license.getId(), ctx.channel());
    }

    @Override
    public String name() {
        return ProxyDataTypeEnum.AUTH.getName();
    }

}
