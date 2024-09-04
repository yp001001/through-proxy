package org.yp.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yp.throughproxy.server.dal.entity.PortPool;

/**
 * @author: yp
 * @date: 2024/8/30 15:25
 * @description:
 */
@Mapper
public interface PortPoolMapper extends BaseMapper<PortPool> {
}
