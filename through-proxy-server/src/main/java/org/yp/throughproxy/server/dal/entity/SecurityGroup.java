package org.yp.throughproxy.server.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: yp
 * @date: 2024/8/30 14:19
 * @description:
 */
@Data
@ToString
@Accessors
@TableName("security_group")
public class SecurityGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    // 组名
    private String name;

    // 描述
    private String description;

    // 用户id
    private Integer userId;

    // 启用状态
    private Integer enable;

    // 默认放行状态
    private Integer defaultPassType;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
