package org.yp.throughproxy.server.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.yp.throughproxy.server.base.db.DBInitialize;
import org.yp.throughproxy.server.base.proxy.ProxyConfig;
import org.yp.throughproxy.server.bo.FlowLimitBO;
import org.yp.throughproxy.server.dal.LicenseMapper;
import org.yp.throughproxy.server.dal.PortMappingMapper;
import org.yp.throughproxy.server.dal.PortPoolMapper;
import org.yp.throughproxy.server.dal.UserMapper;
import org.yp.throughproxy.server.dal.entity.PortMapping;
import org.yp.throughproxy.server.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author: yp
 * @date: 2024/8/30 15:03
 * @description:
 */
@Component
public class PortMappingService implements LifecycleBean {

    @Db
    private PortMappingMapper portMappingMapper;

    @Db
    private LicenseMapper licenseMapper;

    @Db
    private UserMapper userMapper;

    @Db
    private PortPoolMapper portPoolMapper;

    @Inject
    private VisitorChannelService visitorChannelService;

//    @Inject
//    private PortPoolService portPoolService;

    @Inject
    private ProxyConfig proxyConfig;

    @Inject
    private DBInitialize dbInitialize;

    @Inject
    private LicenseService licenseService;

    /** 端口到安全组Id的映射 */
    private final Map<Integer, Integer> mappingPortToSecurityGroupMap = new ConcurrentHashMap<>();
    // 服务端端口到端口映射id的映射
    private final Cache<Integer, Integer> serverPortToPortMappingIdCache = CacheUtil.newLRUCache(500, 1000 * 60 * 10);
    // 端口映射id到licenseId
    private final Cache<Integer, Integer> idToLicenseIdCache = CacheUtil.newLRUCache(500, 1000 * 60 * 10);
    // 流量限制缓存
    private final Cache<Integer, FlowLimitBO> flowLimitCache = CacheUtil.newLRUCache(500, 1000 * 60 * 5);

    @Override
    public void start() throws Throwable {

    }

    public Integer getSecurityGroupIdByMappingPort(int port) {
        return portMappingMapper.querySecurityGroupIdByServerPort(port);
    }

    public FlowLimitBO getFlowLimitByServerPort(Integer serverPort) {
        Integer id = getPortMappingIdByServerPort(serverPort);
        if (null == id) {
            return null;
        }
        FlowLimitBO res = getFlowLimit(id);
        if (null == res || (null == res.getUpLimitRate() && null == res.getDownLimitRate())) {
            Integer licenseId = getLicenseIdById(id);
            if (null != licenseId) {
                res = licenseService.getFlowLimit(licenseId);
            }
        }
        return res;
    }

    public Integer getPortMappingIdByServerPort(Integer serverPort) {
        if (null == serverPort) {
            return null;
        }
        Integer id = serverPortToPortMappingIdCache.get(serverPort);
        if (null != id) {
            return id;
        }
        List<PortMapping> portMappingList = portMappingMapper.findListByServerPort(serverPort);
        // 不存在 或者 有多条记录，都不处理
        if (CollectionUtils.isEmpty(portMappingList) || portMappingList.size() > 1) {
            return null;
        }
        id = portMappingList.get(0).getId();
        serverPortToPortMappingIdCache.put(serverPort, id);
        return id;
    }

    public Integer getLicenseIdById(Integer id) {
        Integer licenseId = idToLicenseIdCache.get(id);
        if (null == licenseId) {
            PortMapping portMapping = portMappingMapper.findById(id);
            if (null != portMapping) {
                licenseId = portMapping.getLicenseId();
                idToLicenseIdCache.put(id, licenseId);
            }
        }
        return licenseId;
    }

    /**
     * 获取license的流量限制
     * @param id
     * @return
     */
    public FlowLimitBO getFlowLimit(Integer id) {
        FlowLimitBO res = flowLimitCache.get(id);
        if (null == res) {
            PortMapping portMapping = portMappingMapper.findById(id);
            if (null != portMapping) {
                refreshFlowLimitCache(id, portMapping.getUpLimitRate(), portMapping.getDownLimitRate());
                res = flowLimitCache.get(id);
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


    public PortMapping queryByLicenseIdAndServerPort(Integer licenseId, int serverPort) {
        return portMappingMapper.queryByLicenseIdAndServerPort(licenseId, serverPort);
    }
}
