package org.dromara.throughproxy.server.base.proxy.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.throughproxy.server.constant.NetworkProtocolEnum;

/**
 * @program: through-proxy
 * @description: 被代理端信息
 * @author: yp
 * @create: 2024-08-31 14:21
 **/

@Accessors(chain = true)
@Data
public class VisitorChannelAttachInfo {

    private NetworkProtocolEnum protocol;
    private String visitorId;
    private String lanInfo;
    private Integer serverPort;
    private String ip;
    private Integer licenseId;

}
