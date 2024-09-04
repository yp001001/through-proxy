package org.yp.throughproxy.server.proxy.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: yp
 * @date: 2024/8/30 13:45
 * @description:
 */
@Data
public class Metrics implements Serializable {

    private static final long serialVersionUID = 1L;
    private int port;
    private long readBytes;
    private long writeBytes;
    private long readMsgs;
    private long writeMsgs;
    private int channels;
    private long timestamps;
}
