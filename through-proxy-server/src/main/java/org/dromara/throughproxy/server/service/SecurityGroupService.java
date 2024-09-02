package org.dromara.throughproxy.server.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.constant.EnableStatusEnum;
import org.dromara.throughproxy.server.constant.SecurityRulePassTypeEnum;
import org.dromara.throughproxy.server.dal.SecurityGroupMapper;
import org.dromara.throughproxy.server.dal.SecurityRuleMapper;
import org.dromara.throughproxy.server.dal.entity.SecurityGroup;
import org.dromara.throughproxy.server.dal.entity.SecurityRule;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.core.runtime.NativeDetector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yp
 * @date: 2024/8/30 14:46
 * @description:
 */
@Component
@Slf4j
public class SecurityGroupService {

    @Db
    private SecurityGroupMapper securityGroupMapper;
    @Db
    private SecurityRuleMapper securityRuleMapper;

    private final Map<Integer, SecurityGroup> securityGroupMap = new ConcurrentHashMap<>();

    // 允许通过控制的缓存，缓存类型最近最久未使用缓存，容量100，超时时间5分钟
    private final Cache<String, Boolean> ipAllowControlCache = CacheUtil.newLRUCache(100, 1000 * 60 * 5);

    @Init(index = 100)
    public void init() {
        // aot阶段，不初始化
        if (NativeDetector.isAotRuntime()) {
            return;
        }
        if(true) return;
        securityGroupMap.clear();
        List<SecurityGroup> securityGroups = securityGroupMapper.selectList(Wrappers.lambdaQuery(SecurityGroup.class)
                .eq(SecurityGroup::getEnable, EnableStatusEnum.ENABLE.getStatus()));
        securityGroups.forEach(securityGroup -> securityGroupMap.put(securityGroup.getId(), securityGroup));
        ipAllowControlCache.clear();
    }

    public void clearCache() {
        ipAllowControlCache.clear();
    }

    public boolean judgeAllow(String remoteIp, Integer securityGroupId) {

        remoteIp = remoteIp.toLowerCase();

        if (StrUtil.isEmpty(remoteIp)) {
            log.debug("[SecurityGroup] cannot get remote ip,this pack be reject");
            return false;
        }

        // 没有该安全组，放行
        if (null == securityGroupId) {
            return true;
        }

        SecurityGroup securityGroup = securityGroupMap.get(securityGroupId);
        if (Objects.isNull(securityGroup)) {
            return true;
        }

        List<SecurityRule> securityRules = securityRuleMapper.queryBySecurityGroupId(securityGroupId);

        Boolean allow = null;

        for (SecurityRule securityRule : securityRules) {
            String rule = securityRule.getRule();
            if (StrUtil.isNotBlank(rule)) {

                SecurityRulePassTypeEnum rulePassTypeEnum = securityRule.judge(remoteIp);

                if (SecurityRulePassTypeEnum.DENY.equals(rulePassTypeEnum)) {
                    log.info("[SecurityGroup] ip:{} groupId:{} ruleId:{} security strategy:{}", remoteIp, securityGroupId, securityRule.getId(), "reject");
                    allow = false;
                    break;
                }

                if(SecurityRulePassTypeEnum.ALLOW.equals(rulePassTypeEnum)){
                    log.debug("[SecurityGroup] ip:{} groupId:{} ruleId:{} security strategy:{}", remoteIp, securityGroupId, securityRule.getId(), "allow");
                    allow = true;
                }
            }
        }

        // 当前IP没有匹配到任何一条规则，则使用安全组默认规则
        if(allow == null){
            allow = SecurityRulePassTypeEnum.ALLOW.getType().equals(securityGroup.getDefaultPassType());
            log.debug("[SecurityGroup] ip:{} groupId{} use security group default strategy:{}", remoteIp, securityGroupId, allow ? "allow" : "reject");
        }

        return allow;
    }
}
