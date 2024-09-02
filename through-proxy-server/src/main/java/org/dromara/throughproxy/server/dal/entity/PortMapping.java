package org.dromara.throughproxy.server.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: yp
 * @date: 2024/8/30 15:12
 * @description:端口映射
 */
@ToString
@Accessors(chain = true)
@Data
@TableName("port_mapping")
public class PortMapping {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer licenseId;

    // 服务端端口
    private Integer serverPort;

    private String protocol;

    // 子域名
    private String subdomain;


    // 客户端ip
    private String clientIp;

    // 客户端端口
    private Integer clientPort;

    // 上传限速
    private String upLimitRate;

    // 下载限速
    private String downLimitRate;

    // 描述
    private String description;

    // 是否在线
    private Integer isOnline;

    // 代理响应数量
    private Integer proxyResponses;

    // 代理超时时间
    private Long proxyTimeoutMs;

    // 安全组Id
    private Integer securityGroupId;

    // 启用状态
    private Integer enable;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
