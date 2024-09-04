package org.yp.throughproxy.server.base.proxy.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.yp.throughproxy.server.constant.NetworkProtocolEnum;

/**
 * @program: through-proxy
 * @description:
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
