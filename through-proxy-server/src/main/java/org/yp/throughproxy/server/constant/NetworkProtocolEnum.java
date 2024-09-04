package org.yp.throughproxy.server.constant;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum NetworkProtocolEnum {

    TCP("TCP", "TCP"),
    UDP("UDP", "UDP"),
    HTTP("HTTP", "TCP"),
    ;
    private String desc;
    private String baseProtocol;

    private static final Map<String, NetworkProtocolEnum> cache =
            Arrays.stream(NetworkProtocolEnum.values()).collect(Collectors.toMap(NetworkProtocolEnum::getDesc, Function.identity()));

    public static NetworkProtocolEnum of(String desc){
        if(StrUtil.isBlank(desc)){
            return null;
        }

        if("http".equalsIgnoreCase(desc)){
            return HTTP;
        }

        return cache.get(desc);
    }


}
