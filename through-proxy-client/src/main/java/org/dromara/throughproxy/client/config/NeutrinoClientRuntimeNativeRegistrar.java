package org.dromara.throughproxy.client.config;

import org.noear.solon.annotation.Component;
import org.noear.solon.aot.RuntimeNativeMetadata;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.core.AppContext;

/**
 * @program: through-proxy
 * @description:
 * @author: yp
 * @create: 2024-08-29 20:49
 **/
@Component
public class NeutrinoClientRuntimeNativeRegistrar implements RuntimeNativeRegistrar {
    @Override
    public void register(AppContext context, RuntimeNativeMetadata metadata) {

    }
}
