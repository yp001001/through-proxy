package org.dromara.throughproxy.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: yp
 * @date: 2024/8/30 14:55
 * @description:
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum {
    ENABLE(1, "启用"),
    DISABLE(2, "禁用");
    private static Map<Integer, EnableStatusEnum> CACHE =
            Arrays.stream(values()).collect(Collectors.toMap(EnableStatusEnum::getStatus, Function.identity()));
    private Integer status;
    private String desc;
    public static EnableStatusEnum of(Integer status) {
        return CACHE.get(status);
    }
}
