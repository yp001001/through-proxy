package org.dromara.throughproxy.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: yp
 * @date: 2024/9/2 11:00
 * @description:
 */
@Getter
@AllArgsConstructor
public enum ClientConnectTypeEnum {
    CONNECT(1, "连接"),
    DISCONNECT(2, "断开连接");

    private Integer type;
    private String desc;
}
