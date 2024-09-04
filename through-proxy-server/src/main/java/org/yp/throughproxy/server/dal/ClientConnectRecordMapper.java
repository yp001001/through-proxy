package org.yp.throughproxy.server.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.yp.throughproxy.server.dal.entity.ClientConnectRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yp
 * @date: 2024/9/2 10:58
 * @description:
 */
@Mapper
public interface ClientConnectRecordMapper extends BaseMapper<ClientConnectRecord> {

    List<ClientConnectRecord> clientConnectRecordList = new ArrayList<>();

}
