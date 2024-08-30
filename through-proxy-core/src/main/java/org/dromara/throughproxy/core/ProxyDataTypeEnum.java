package org.dromara.throughproxy.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: yp
 * @date: 2024/8/30 11:01
 * @description:
 */
@Getter
@AllArgsConstructor
public enum ProxyDataTypeEnum {

    HEARTBEAT(0x01, Constants.ProxyDataTypeName.HEARTBEAT, "HEARTBEAT"),
    ;

    private static final Map<Integer, ProxyDataTypeEnum> cache =
            Arrays.stream(values()).collect(Collectors.toMap(ProxyDataTypeEnum::getType, Function.identity()));

    private int type;
    private String name;
    private String desc;

    public static ProxyDataTypeEnum of(Integer type){
        return cache.get(type);
    }

}
