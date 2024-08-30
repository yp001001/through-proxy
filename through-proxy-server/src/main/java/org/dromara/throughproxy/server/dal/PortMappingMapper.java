package org.dromara.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.throughproxy.server.dal.entity.PortMapping;

import java.util.List;
import java.util.Objects;

/**
 * @author: yp
 * @date: 2024/8/30 15:18
 * @description:
 */
@Mapper
public interface PortMappingMapper extends BaseMapper<PortMapping> {

    default Integer querySecurityGroupIdByServerPort(int serverPort) {
        LambdaQueryWrapper<PortMapping> queryWrapper = new LambdaQueryWrapper<PortMapping>()
                .eq(PortMapping::getServerPort, serverPort);
        PortMapping portMapping = this.selectOne(queryWrapper);
        if (!Objects.isNull(portMapping)) {
            return portMapping.getSecurityGroupId();
        }
        return null;
    }

    default PortMapping findById(Integer id) {
        return this.findById(id);
    }

    default List<PortMapping> findListByServerPort(Integer serverPort) {
        return this.selectList(new LambdaQueryWrapper<PortMapping>()
                .eq(PortMapping::getServerPort, serverPort)
        );
    }
}
