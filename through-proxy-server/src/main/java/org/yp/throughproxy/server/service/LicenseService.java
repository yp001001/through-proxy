package org.yp.throughproxy.server.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.noear.solon.core.runtime.NativeDetector;
import org.yp.throughproxy.server.base.db.DBInitialize;
import org.yp.throughproxy.server.bo.FlowLimitBO;
import org.yp.throughproxy.server.dal.LicenseMapper;
import org.yp.throughproxy.server.dal.PortMappingMapper;
import org.yp.throughproxy.server.dal.UserMapper;
import org.yp.throughproxy.server.dal.entity.License;
import org.yp.throughproxy.server.util.StringUtil;

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

    @Init
    public void init(){
        // aot阶段，不初始化
        if(NativeDetector.isAotRuntime()){
            return;
        }
        // 服务刚启动，默认所有license都是离线状态，解决服务突然关闭，在线状态来不及更新的问题
//        licenseMapper.updateOnlineStatus(OnlineStatusEnum.OFFLINE.getStatus(), new Date());
        // 刷新流量限制缓存
//        List<License> licenseList = licenseMapper.listAll();
//        if(!CollectionUtils.isEmpty(licenseList)){
//            for (License license : licenseList) {
//                refreshFlowLimitCache(license.getId(), license.getUpLimitRate(), license.getDownLimitRate());
//            }
//        }
    }


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

    public License queryByKey(String licenseKey) {
        return licenseMapper.queryByLicenseKey(licenseKey);
    }
}
