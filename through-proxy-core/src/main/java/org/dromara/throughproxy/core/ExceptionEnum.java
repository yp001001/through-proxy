package org.dromara.throughproxy.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: yp
 * @date: 2024/8/30 9:36
 * @description:
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    SUCCESS(0, "success"),
    AUTH_FAILED(1, "auth failed"),
    CONNECT_FAILED(2, "connect failed"),
    @Deprecated
    LICENSE_CANNOT_REPEAT_CONNECT(3, "license cannot multiple client simultaneous use"),
    ;

    private Integer code;
    private String msg;
}