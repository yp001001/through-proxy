package org.dromara.throughproxy.server.base.db;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.solon.annotation.Db;
import org.dromara.throughproxy.server.constant.DbTypeEnum;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.solon.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.solon.plugins.inner.PaginationInnerInterceptor;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

/**
 * @program: through-proxy
 * @description: 数据库池创建以及配置
 * @author: yp
 * @create: 2024-08-29 22:42
 **/

@Configuration
public class DbCofniguration {

    @Bean(value = "db", typed = true)
    public DataSource dataSource(@Inject DbConfig dbConfig) {
        DbTypeEnum dbTypeEnum = DbTypeEnum.of(dbConfig.getType());
        if (DbTypeEnum.H2 == dbTypeEnum) {
            return newHikariDataSource(dbConfig, "org.h2.Driver");
        } else if (DbTypeEnum.MYSQL == dbTypeEnum) {
            return newHikariDataSource(dbConfig, getMysqlDriver());
        } else if (DbTypeEnum.MARIADB == dbTypeEnum) {
            return newHikariDataSource(dbConfig, "org.mariadb.jdbc.Driver");
        } else {
            throw new RuntimeException("datasource type must not null...");
        }
    }

    private String getMysqlDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return "com.mysql.cj.jdbc.Driver";
        } catch (ClassNotFoundException e) {
            // 适配mysql5
            return "com.mysql.jdbc.Driver";
        }
    }

    private DataSource newHikariDataSource(DbConfig dbConfig, String driverClass) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(driverClass);
        hikariDataSource.setUsername(dbConfig.getUsername());
        hikariDataSource.setPassword(dbConfig.getPassword());
        hikariDataSource.setJdbcUrl(dbConfig.getUrl());
        // 待配置文件设置
        hikariDataSource.setMinimumIdle(5);
        hikariDataSource.setMaximumPoolSize(20);
        hikariDataSource.setMaxLifetime(60000);
        return hikariDataSource;
    }


    @Bean
    public void dbExt(@Db("db")MybatisConfiguration config){
        config.getTypeHandlerRegistry().register("org.dromara.neutrinoproxy.server.dal.entity");
        config.setDefaultEnumTypeHandler(null);

        MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
        plusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        config.addInterceptor(plusInterceptor);
    }

    @Bean
    public MybatisSqlSessionFactoryBuilder factoryBuilderNew() {
        return new MybatisSqlSessionFactoryBuilderImpl();
    }

}
