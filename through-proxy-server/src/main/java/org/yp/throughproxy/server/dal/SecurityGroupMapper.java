package org.yp.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yp.throughproxy.server.dal.entity.SecurityGroup;

/**
 * @author: yp
 * @date: 2024/8/30 14:18
 * @description:
 */
@Mapper
public interface SecurityGroupMapper extends BaseMapper<SecurityGroup> {
}
