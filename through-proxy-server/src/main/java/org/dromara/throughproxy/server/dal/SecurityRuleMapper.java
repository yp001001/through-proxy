package org.dromara.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.throughproxy.server.constant.EnableStatusEnum;
import org.dromara.throughproxy.server.dal.entity.SecurityRule;

import java.util.List;

/**
 * @author: yp
 * @date: 2024/8/30 14:42
 * @description:
 */
@Mapper
public interface SecurityRuleMapper extends BaseMapper<SecurityRule> {

    default List<SecurityRule> queryBySecurityGroupId(Integer securityGroupId) {
        return this.selectList(Wrappers.lambdaQuery(SecurityRule.class)
                .eq(SecurityRule::getGroupId, securityGroupId)
                .eq(SecurityRule::getEnable, EnableStatusEnum.ENABLE)
                .orderByAsc(SecurityRule::getPriority));
    }

}
