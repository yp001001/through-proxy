package org.dromara.throughproxy.server.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.base.db.DBInitialize;
import org.dromara.throughproxy.server.bo.FlowLimitBO;
import org.dromara.throughproxy.server.dal.LicenseMapper;
import org.dromara.throughproxy.server.dal.PortMappingMapper;
import org.dromara.throughproxy.server.dal.UserMapper;
import org.dromara.throughproxy.server.dal.entity.License;
import org.dromara.throughproxy.server.util.StringUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;

;

/**
 * @author: yp
 * @date: 2024/8/30 17:40
 * @description:
 */
@Component
public class LicenseService implements LifecycleBean {

    @Db
    private LicenseMapper licenseMapper;
    @Db
    private PortMappingMapper portMappingMapper;
    @Db
    private UserMapper userMapper;
    @Inject
    private VisitorChannelService visitorChannelService;
    @Inject
    private DBInitialize dbInitialize;
    // 流量限制缓存
    private final Cache<Integer, FlowLimitBO> flowLimitCache = CacheUtil.newLRUCache(200, 1000 * 60 * 5);

    @Override
    public void start() throws Throwable {

    }

    /**
     * 获取license的流量限制
     * @param licenseId
     * @return
     */
    public FlowLimitBO getFlowLimit(Integer licenseId) {
        FlowLimitBO res = flowLimitCache.get(licenseId);
        if (null == res) {
            License license = licenseMapper.queryById(licenseId);
            if (null != license) {
                refreshFlowLimitCache(licenseId, license.getUpLimitRate(), license.getDownLimitRate());
                res = flowLimitCache.get(licenseId);
            }
        }
        return res;
    }

    /**
     * 刷新流量限制缓存
     * @param id
     * @param upLimitRate
     * @param downLimitRate
     */
    private void refreshFlowLimitCache(Integer id, String upLimitRate, String downLimitRate) {
        if (null == id) {
            return;
        }
        flowLimitCache.put(id, new FlowLimitBO()
                .setUpLimitRate(StringUtil.parseBytes(upLimitRate))
                .setDownLimitRate(StringUtil.parseBytes(downLimitRate))
        );
    }
}
