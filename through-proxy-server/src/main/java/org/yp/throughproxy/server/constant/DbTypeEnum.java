package org.yp.throughproxy.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @program: through-proxy
 * @description: 数据库类型枚举
 * @author: yp
 * @create: 2024-08-29 22:47
 **/

@Getter
@AllArgsConstructor
public enum DbTypeEnum {

    H2("h2"),
    MYSQL("mysql"),
    MARIADB("mariadb"),
    ;

    private String type;

    private static final Map<String, DbTypeEnum> cache
            = Stream.of(DbTypeEnum.values()).collect(Collectors.toMap(DbTypeEnum::getType, Function.identity()));

    public static DbTypeEnum of(String type) {
        return cache.get(type);
    }

}
