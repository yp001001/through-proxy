package org.dromara.throughproxy.core;

import java.util.HashMap;

/**
 * @author: yp
 * @date: 2024/8/30 18:15
 * @description:
 */
public class ChannelAttribute extends HashMap<String, Object> {

    public static ChannelAttribute create() {
        return new ChannelAttribute();
    }

    public static ChannelAttribute of(String k, Object v) {
        ChannelAttribute channelAttr = create();
        channelAttr.put(k, v);

        return channelAttr;
    }

    public ChannelAttribute set(String k, Object v) {
        super.put(k, v);
        return this;
    }

    public <T> T get(String key) {
        return (T)super.get(key);
    }

    public String getString(String k) {
        return String.valueOf(this.get(k));
    }

    public Long getLong(String k) {
        return Long.valueOf(String.valueOf(this.get(k)));
    }

}
