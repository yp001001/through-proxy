package org.dromara.throughproxy.server.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: yp
 * @date: 2024/8/30 17:34
 * @description:
 */
@Accessors(chain = true)
@Data
public class FlowLimitBO {

    private Long upLimitRate;
    private Long downLimitRate;

}
