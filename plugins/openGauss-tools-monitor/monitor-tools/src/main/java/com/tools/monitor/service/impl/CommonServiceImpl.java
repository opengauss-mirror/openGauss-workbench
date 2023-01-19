package com.tools.monitor.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.tools.monitor.entity.DataSource;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.exception.ParamsException;
import com.tools.monitor.mapper.SysConfigMapper;
import com.tools.monitor.util.AssertUtil;
import com.tools.monitor.util.Base64;
import com.tools.monitor.util.jdbc.JdbcUtil;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

/**
 * CommonServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Service
public class CommonServiceImpl {
    @Autowired
    private SysConfigMapper configMapper;


    /**
     * getDataSource
     *
     * @return DataSource
     */
    public DataSource getDataSource() {
        SysConfig sysConfig = configMapper.getZabbixConfig();
        return JdbcUtil.getDataSource(sysConfig);
    }

    /**
     * executeSql
     *
     * @param jdbcTemplate
     * @param sql
     * @return list
     * @throws ParamsException
     */
    public List<Map<String, Object>> executeSql(JdbcTemplate jdbcTemplate, String sql) throws ParamsException{
            return jdbcTemplate.queryForList(sql);
    }

    /**
     * executeSingleSql
     *
     * @param jdbcTemplate
     * @param sql
     * @param clazz
     * @return t
     * @param <T>
     */
    public <T> T executeSingleSql(JdbcTemplate jdbcTemplate, String sql, Class<T> clazz) {
        try {
            return jdbcTemplate.queryForObject(sql, clazz);
        } catch (Exception exception) {
            log.error("executeSql error,{}", exception.getMessage());
            throw new ParamsException(exception.getMessage());
        }
    }

    /**
     * getZabbixTem
     *
     * @return JdbcTemplate
     */
    public JdbcTemplate getZabbixTem() {
        SysConfig sysConfig = configMapper.getZabbixConfig();
        AssertUtil.isTrue(ObjectUtil.isEmpty(sysConfig), "请先配置zabbix数据库");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(sysConfig.getUrl());
        dataSource.setUsername(sysConfig.getUserName());
        dataSource.setPassword(sysConfig.getPassword());
        return new JdbcTemplate(dataSource);
    }

    /**
     * getTem
     *
     * @param sysConfig
     * @return JdbcTemplate
     */
    public JdbcTemplate getTem(SysConfig sysConfig) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(sysConfig.getUrl());
        dataSource.setUsername(sysConfig.getUserName());
        dataSource.setPassword(Base64.decode(sysConfig.getPassword()));
        return new JdbcTemplate(dataSource);
    }
}
