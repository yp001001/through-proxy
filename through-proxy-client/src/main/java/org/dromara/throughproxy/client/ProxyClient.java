package org.dromara.throughproxy.client;

import org.noear.solon.Solon;

/**
 * @program: through-proxy
 * @description: 启动类
 * @author: yp
 * @create: 2024-09-01 14:59
 **/
public class ProxyClient {

    public static void main(String[] args) {
        Solon.start(ProxyClient.class, args);
    }

}
