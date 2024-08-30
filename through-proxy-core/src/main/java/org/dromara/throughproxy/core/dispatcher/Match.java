package org.dromara.throughproxy.core.dispatcher;

import java.lang.annotation.*;

/**
 * @author: yp
 * @date: 2024/8/30 9:38
 * @description:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Match {
    String type();
}

