package org.dromara.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.throughproxy.server.dal.entity.License;

/**
 * @author: yp
 * @date: 2024/8/30 15:20
 * @description:
 */
@Mapper
public interface LicenseMapper extends BaseMapper<License> {
    default License queryById(Integer licenseId){
        return this.queryById(licenseId);
    }
}
