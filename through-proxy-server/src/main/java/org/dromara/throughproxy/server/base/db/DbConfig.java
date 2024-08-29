package org.dromara.throughproxy.server.base.db;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * @program: through-proxy
 * @description: 数据库配置
 * @author: yp
 * @create: 2024-08-29 22:40
 **/
@Component
@Data
public class DbConfig {

    /**
     * 数据库类型
     * {@link DbTypeEnum}
     */
    @Inject("${through.data.db.type}")
    private String type;
    /**
     * 连接url
     */
    @Inject("${through.data.db.url}")
    private String url;

    /**
     * 用户名
     */
    @Inject("${through.data.db.username}")
    private String username;
    /**
     * 密码
     */
    @Inject("${through.data.db.password}")
    private String password;

}
