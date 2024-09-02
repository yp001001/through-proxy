package org.dromara.throughproxy.server.proxy.domain;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.throughproxy.server.dal.entity.PortMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yp
 * @date: 2024/9/2 11:23
 * @description:
 */
@Accessors(chain = true)
@Data
public class ProxyMapping {
    /**
     * 服务端端口
     */
    private Integer serverPort;
    /**
     * 客户端信息 ip:port
     */
    private String lanInfo;

    public static List<ProxyMapping> buildList(List<PortMapping> portMappingList) {
        List<ProxyMapping> list = new ArrayList<>();
        if (CollectionUtil.isEmpty(portMappingList)) {
            return list;
        }
        for (PortMapping portMapping : portMappingList) {
            list.add(build(portMapping));
        }
        return list;
    }

    public static ProxyMapping build(PortMapping portMapping) {
        return new ProxyMapping()
                .setServerPort(portMapping.getServerPort())
                .setLanInfo(String.format("%s:%s", portMapping.getClientIp(), portMapping.getClientPort()));
    }
}
