package org.yp.throughproxy.server.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.yp.throughproxy.server.constant.EnableStatusEnum;

import java.util.Date;

/**
 * @author: yp
 * @date: 2024/8/30 15:19
 * @description:许可证
 */
@ToString
@Accessors(chain = true)
@Data
@TableName("license")
public class License {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * licenseKey
     */
    @TableField("`key`")
    private String key;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 上传限速
     */
    private String upLimitRate;
    /**
     * 下载限速
     */
    private String downLimitRate;
    /**
     * 是否在线
     * {@link OnlineStatusEnum}
     */
    private Integer isOnline;
    /**
     * 启用状态
     * {@link EnableStatusEnum}
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
