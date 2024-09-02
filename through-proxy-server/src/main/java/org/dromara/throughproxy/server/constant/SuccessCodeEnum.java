package org.dromara.throughproxy.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: yp
 * @date: 2024/9/2 11:01
 * @description:
 */
@Getter
@AllArgsConstructor
public enum SuccessCodeEnum {
    SUCCESS(1, "成功"),
    FAIL(2, "失败");

    private Integer code;
    private String desc;
}
