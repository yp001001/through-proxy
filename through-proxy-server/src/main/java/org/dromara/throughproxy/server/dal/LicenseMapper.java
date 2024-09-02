package org.dromara.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.throughproxy.server.dal.entity.License;

import java.util.Date;
import java.util.List;

/**
 * @author: yp
 * @date: 2024/8/30 15:20
 * @description:
 */
@Mapper
public interface LicenseMapper extends BaseMapper<License> {

    default License queryById(Integer licenseId){
        return this.selectOne(new LambdaQueryWrapper<License>()
                .eq(License::getId, licenseId));
    }

    default void updateOnlineStatus(Integer status, Date updateTime){
        this.update(null, new LambdaUpdateWrapper<License>()
                .set(License::getIsOnline, status)
                .set(License::getUpdateTime, updateTime));
    }

    default List<License> listAll(){
        return this.selectList(null);
    }

    default License queryByLicenseKey(String licenseKey){
        return this.selectOne(new LambdaQueryWrapper<License>()
                .eq(License::getKey, licenseKey));
    }

    default void updateOnlineStatus(Integer id, Integer status, Date updateTime){
        this.update(null, new LambdaUpdateWrapper<License>()
                .eq(License::getId, id)
                .set(License::getIsOnline, status)
                .set(License::getUpdateTime, updateTime));
    }
}
