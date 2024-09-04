package org.yp.throughproxy.core.dispatcher;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.yp.throughproxy.core.ProxyMessageHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author: yp
 * @date: 2024/8/30 9:42
 * @description:消息处理分发接口实现类
 */
@Slf4j
public class DefaultDispatcher<Context, Data> implements Dispatcher<Context, Data> {

    /**
     * 调度器名称
     */
    private String name;

    /**
     * 缓存所有消息处理器
     */
    private Map<String, Handler<Context, Data>> handlerMap = new HashMap<>();

    /**
     * 获取对应handler函数接口
     */
    private Function<Data, String> matcher;


    public DefaultDispatcher(String name, Function<Data, String> matcher) {
        Assert.notNull(name, "name cannot empty!");
        Assert.notNull(matcher, "matcher cannot empty!");
        this.name = name;
        this.matcher = matcher;
        List<ProxyMessageHandler> proxyMessageHandlerList = Solon.context().getBeansOfType(ProxyMessageHandler.class);
        if (!CollectionUtil.isEmpty(proxyMessageHandlerList)) {
            for (ProxyMessageHandler proxyMessageHandler : proxyMessageHandlerList) {
                Match match = proxyMessageHandler.getClass().getAnnotation(Match.class);
                handlerMap.put(match.type(), (Handler<Context, Data>) proxyMessageHandler);
            }
        }
    }


    @Override
    public void dispatch(Context context, Data data) {
        Handler<Context, Data> handler = handlerMap.get(matcher.apply(data));
        if(Objects.isNull(handler)){
            log.warn("proxymessageHandler is error, name is {}", matcher.apply(data));
            return;
        }
        handler.handle(context, data);
    }

}
