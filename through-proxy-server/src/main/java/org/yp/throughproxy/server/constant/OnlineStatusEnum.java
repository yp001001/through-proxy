package org.yp.throughproxy.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: yp
 * @date: 2024/9/2 10:47
 * @description:在线枚举状态
 */
@Getter
@AllArgsConstructor
public enum OnlineStatusEnum {
    ONLINE(1, "在线"),
    OFFLINE(2, "离线");

    private Integer status;
    private String desc;
}
