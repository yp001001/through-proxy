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
 * @date: 2024/8/30 15:22
 * @description:端口池
 */
@ToString
@Accessors(chain = true)
@Data
@TableName("port_pool")
public class PortPool {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分组id
     */
    private Integer groupId;

    /**
     * 端口
     */
    private Integer port;
    /**
     * 是否禁用
     */
    private Integer enable;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
