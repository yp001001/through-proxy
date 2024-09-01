package org.dromara.throughproxy.server;

import org.noear.solon.Solon;

/**
 * @program: through-proxy
 * @description: 启动类
 * @author: yp
 * @create: 2024-09-01 14:59
 **/
public class ProxyServer {

    public static void main(String[] args) {
        Solon.start(ProxyServer.class, args);
    }

}
