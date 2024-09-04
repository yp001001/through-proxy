package org.yp.throughproxy.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: yp
 * @date: 2024/8/30 14:27
 * @description:
 */
@AllArgsConstructor
@Getter
public enum SecurityRulePassTypeEnum {
    DENY(0, "deny"),
    ALLOW(1, "allow"),
    NONE(-1, "none")
    ;

    private final Integer type;
    private final String desc;
}
