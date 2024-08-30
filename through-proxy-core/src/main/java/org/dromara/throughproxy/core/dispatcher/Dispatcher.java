package org.dromara.throughproxy.core.dispatcher;

/**
 * @author: yp
 * @date: 2024/8/30 9:41
 * @description:消息处理分发接口
 */
public interface Dispatcher<Context, Data>{

    void dispatch(Context context, Data data);

}
