package org.yp.throughproxy.core.dispatcher;

/**
 * @author: yp
 * @date: 2024/8/30 9:25
 * @description:
 */
public interface Handler<Context, Data> {

    /**
     * 处理
     * @param context
     * @param data
     */
    void handle(Context context, Data data);


    /**
     * 名称
     * @return
     */
    String name();

}
