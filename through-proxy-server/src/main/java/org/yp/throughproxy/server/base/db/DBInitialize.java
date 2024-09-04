package org.yp.throughproxy.server.base.db;

/**
 * @program: through-proxy
 * @description: 初始化数据库
 * @author: yp
 * @create: 2024-08-29 23:11
 **/

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.wood.DbContext;
import org.noear.wood.annotation.Db;
import org.yp.throughproxy.core.util.FileUtil;
import org.yp.throughproxy.server.constant.DbTypeEnum;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class DBInitialize implements EventListener<AppLoadEndEvent> {

    private static List<String> initDataTableNameList = Arrays.asList("user", "license", "port_group", "port_pool", "port_mapping", "job_info");
    @Inject
    private DbConfig dbConfig;

    private DbTypeEnum dbTypeEnum;

    @Db
    DbContext dbContext;

    @Init
    public void init() throws Throwable {
        // aot 阶段，不初始化数据库
        if (NativeDetector.isAotRuntime()) {
            return;
        }
        Assert.notNull(dbConfig.getType(), "neutrino.data.db.type不能为空!");
        dbTypeEnum = DbTypeEnum.of(dbConfig.getType());
        Assert.notNull(dbTypeEnum, "neutrino.data.db.type取值异常!");

        log.info("{} database init...", dbConfig.getType());
        initDBStructure();
        initDBData();
    }

    @Override
    public void onEvent(AppLoadEndEvent appLoadEndEvent) throws Throwable {

    }

    /**
     * 初始化数据库结构
     */
    private void initDBStructure() throws Exception {
        List<String> lines = FileUtil.readContentAsStringList(String.format("classpath:/sql/%s/init-structure.sql", dbConfig.getType()));
        if (CollectionUtil.isEmpty(lines)) {
            return;
        }
        String sql = "";
        for (String line : lines) {
            if (StrUtil.isEmpty(line) || StrUtil.isEmpty(line.trim()) || line.trim().startsWith("#")) {
                continue;
            }
            sql += "\r\n" + line.trim();
            if (sql.endsWith(";")) {
                log.debug("init database table sql:{}", sql);
                dbContext.exe(sql);
                sql = "";
            }
        }
    }

    /**
     * 初始化数据
     *
     * @throws Exception
     */
    private void initDBData() throws Exception {
        if (CollectionUtil.isEmpty(initDataTableNameList)) {
            return;
        }
        for (String tableName : initDataTableNameList) {
            // 表里没有数据的时候，才进行初始化操作

            Object result = dbContext.exe(String.format("select count(1) from `%s`", tableName));
            if (result != null && ((Number) result).intValue() > 0) {
                continue;
            }
            List<String> lines = FileUtil.readContentAsStringList(String.format("classpath:/sql/%s/%s.data.sql", dbConfig.getType(), tableName));
            if (CollectionUtil.isEmpty(lines)) {
                return;
            }
            String sql = "";
            for (String line : lines) {
                if (StrUtil.isEmpty(line) || StrUtil.isEmpty(line.trim()) || line.trim().startsWith("#")) {
                    continue;
                }
                sql += "\r\n" + line.trim();
                if (sql.endsWith(";")) {
                    log.debug("init database data[table={}] sql:{}", tableName, sql);
                    dbContext.exe(sql);
                    sql = "";
                }
            }
        }
    }
}

