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
 * @date: 2024/9/2 10:57
 * @description:
 */
@ToString
@Accessors(chain = true)
@Data
@TableName("client_connect_record")
public class ClientConnectRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String ip;
    private Integer licenseId;
    private Integer type;
    private String msg;
    /**
     * 1、成功
     * 2、失败
     */
    private Integer code;
    private String err;
    /**
     * 创建时间
     */
    private Date createTime;

}
