package org.dromara.throughproxy.client.util;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.throughproxy.client.config.ProxyConfig;
import org.dromara.throughproxy.core.util.FileUtil;
import org.noear.solon.Solon;

import java.util.UUID;

/**
 * @program: through-proxy
 * @description:
 * @author: yp
 * @create: 2024-09-01 14:27
 **/
@Slf4j
public class ProxyUtil {

    private static String clientId;

    private static volatile Channel cmdChannel;

    private static final String CLIENT_ID_FILE = ".NEUTRINO_PROXY_CLIENT_ID";

    public static String getClientId() {
        if (StringUtils.isNotBlank(clientId)) {
            return clientId;
        }
        ProxyConfig proxyConfig = Solon.context().getBean(ProxyConfig.class);
        if (StringUtils.isNotBlank(proxyConfig.getTunnel().getClientId())) {
            clientId = proxyConfig.getTunnel().getClientId();
            return clientId;
        }
        String id = FileUtil.readContentAsString(CLIENT_ID_FILE);
        if (StringUtils.isNotBlank(id)) {
            clientId = id;
            return id;
        }
        id = UUID.randomUUID().toString().replace("-", "");
        FileUtil.write(CLIENT_ID_FILE, id);
        clientId = id;
        return id;
    }

    public static void setCmdChannel(Channel channel) {
        ProxyUtil.cmdChannel = channel;
    }
}
