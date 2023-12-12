/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_DATABASELINK_COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_DATA_CONNECTION_NOT_P_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_DATA_CONNECTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;

/**
 * DatabaseConnectionDAO
 *
 * @since 2023-6-26
 */
@Slf4j
@Repository
public class DatabaseConnectionDAO implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * insert table
     *
     * @param databaseConnectionDO databaseConnectionDO
     */
    public void insertTable(DatabaseConnectionDO databaseConnectionDO) {
        jdbcTemplate.execute(
                "insert into DATABASELINK(type,name,driver,ip,port,dataname,username," +
                        "userpassword,webuser,edition) values('" + databaseConnectionDO.getType()
                        + "','" + databaseConnectionDO.getName() + "','" + databaseConnectionDO.getDriver()
                        + "','" + databaseConnectionDO.getIp() + "','" + databaseConnectionDO.getPort()
                        + "','" + databaseConnectionDO.getDataName() + "','" + databaseConnectionDO.getUserName()
                        + "','" + databaseConnectionDO.getPassword() + "','" + databaseConnectionDO.getWebUser()
                        + "','" + databaseConnectionDO.getEdition() + "');");
    }

    /**
     * update table
     *
     * @param databaseConnectionDO databaseConnectionDO
     */
    public void updateTable(DatabaseConnectionDO databaseConnectionDO) {
        jdbcTemplate.execute(
                "UPDATE DATABASELINK SET type= '" + databaseConnectionDO.getType()
                        + "' ,driver = '" + databaseConnectionDO.getDriver()
                        + "',ip= '" + databaseConnectionDO.getIp()
                        + "' ,port = '" + databaseConnectionDO.getPort()
                        + "',dataName = '" + databaseConnectionDO.getDataName()
                        + "',username ='" + databaseConnectionDO.getUserName()
                        + "' ,userpassword ='" + databaseConnectionDO.getPassword()
                        + "' ,edition ='" + databaseConnectionDO.getEdition()
                        + "'  WHERE name = '" + databaseConnectionDO.getName()
                        + "'  and webuser = '" + databaseConnectionDO.getWebUser() + "';");
    }

    /**
     * select table
     *
     * @param webUser webUser
     * @return List
     */
    public List<DatabaseConnectionDO> selectTable(String webUser) {
        List<DatabaseConnectionDO> list = new ArrayList<>();
        Map<String, Object> count = jdbcTemplate.queryForMap(
                GET_DATABASELINK_COUNT_SQL + " webuser = '" + webUser + "';");
        if (count.get("count") instanceof Integer) {
            if ((Integer) count.get("count") == 0) {
                return null;
            } else {
                list = jdbcTemplate.query(GET_DATA_CONNECTION_NOT_P_SQL + " webuser = '" + webUser + "';",
                        new BeanPropertyRowMapper<>(DatabaseConnectionDO.class));
            }
        }
        return list;
    }

    /**
     * get connection by name
     *
     * @param name name
     * @param webUser webUser
     * @return DatabaseConnectionUrlDO
     */
    public DatabaseConnectionUrlDO getByName(String name, String webUser) {
        DatabaseConnectionUrlDO databaseConnectionUrlDO = new DatabaseConnectionUrlDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(
                GET_DATA_CONNECTION_SQL + " name = '" + name + "' and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        } else {
            databaseConnectionUrlDO.setId((Integer) data.get("id"));
            databaseConnectionUrlDO.setType((String) data.get("type"));
            databaseConnectionUrlDO.setName((String) data.get("name"));
            databaseConnectionUrlDO.setDriver((String) data.get("driver"));
            databaseConnectionUrlDO.setUrl(GET_URL_JDBC + data.get("ip") + ":" + data.get("port") + "/" + data.get(
                    "dataName") + CONFIGURE_TIME);
            databaseConnectionUrlDO.setUserName((String) data.get("username"));
            databaseConnectionUrlDO.setPassword((String) data.get("userpassword"));
            databaseConnectionUrlDO.setWebUser((String) data.get("webuser"));
            return databaseConnectionUrlDO;

        }
    }

    /**
     * get judge name
     *
     * @param name name
     * @param webUser webUser
     * @return Integer
     */
    public Integer getJudgeName(String name, String webUser) {
        Map<String, Object> count = jdbcTemplate.queryForMap(
                GET_DATABASELINK_COUNT_SQL + " name ='" + name + "' and webUser = '" + webUser + "'");
        return (Integer) count.get("count");
    }

    /**
     * delete table
     *
     * @param id id
     */
    public void deleteTable(Integer id) {
        jdbcTemplate.execute("delete from DATABASELINK where id = " + id + ";");
    }

    /**
     * get database by id
     *
     * @param id id
     * @param webUser webUser
     * @return DatabaseConnectionDO
     */
    public DatabaseConnectionDO getByIdDatabase(Integer id, String webUser) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(
                GET_DATA_CONNECTION_SQL + " id =" + id + " and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        } else {
            databaseConnectionDO.setId(String.valueOf(data.get("id")));
            databaseConnectionDO.setType((String) data.get("type"));
            databaseConnectionDO.setName((String) data.get("name"));
            databaseConnectionDO.setDriver((String) data.get("driver"));
            databaseConnectionDO.setIp((String) data.get("ip"));
            databaseConnectionDO.setPort((String) data.get("port"));
            databaseConnectionDO.setUserName((String) data.get("username"));
            databaseConnectionDO.setPassword((String) data.get("userpassword"));
            if (data.get("webuser") instanceof String) {
                databaseConnectionDO.setWebUser((String) data.get("webuser"));
            }
            databaseConnectionDO.setEdition((String) data.get("edition"));
            return databaseConnectionDO;

        }
    }

    /**
     * get attribute by id
     *
     * @param id id
     * @param webUser webUser
     * @return DatabaseConnectionDO
     */
    public DatabaseConnectionDO getAttributeById(String id, String webUser) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(
                GET_DATA_CONNECTION_SQL + " id =" + id + " and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        } else {
            databaseConnectionDO.setId(String.valueOf(data.get("id")));
            databaseConnectionDO.setType((String) data.get("type"));
            databaseConnectionDO.setName((String) data.get("name"));
            databaseConnectionDO.setDriver((String) data.get("driver"));
            databaseConnectionDO.setIp((String) data.get("ip"));
            databaseConnectionDO.setPort((String) data.get("port"));
            databaseConnectionDO.setDataName((String) data.get("dataname"));
            databaseConnectionDO.setUserName((String) data.get("username"));
            if (data.get("webuser") instanceof String) {
                databaseConnectionDO.setWebUser((String) data.get("webuser"));
            }
            return databaseConnectionDO;

        }
    }

    /**
     * get attribute by name
     *
     * @param name name
     * @param webUser webUser
     * @return DatabaseConnectionDO
     */
    public DatabaseConnectionDO getAttributeByName(String name, String webUser) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(
                GET_DATA_CONNECTION_SQL + " name ='" + name + "' and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        } else {
            databaseConnectionDO.setId(String.valueOf(data.get("id")));
            databaseConnectionDO.setType((String) data.get("type"));
            databaseConnectionDO.setName((String) data.get("name"));
            databaseConnectionDO.setDriver((String) data.get("driver"));
            databaseConnectionDO.setIp((String) data.get("ip"));
            databaseConnectionDO.setPort((String) data.get("port"));
            databaseConnectionDO.setDataName((String) data.get("dataname"));
            databaseConnectionDO.setUserName((String) data.get("username"));
            databaseConnectionDO.setPassword((String) data.get("userpassword"));
            databaseConnectionDO.setEdition((String) data.get("edition"));
            return databaseConnectionDO;

        }
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                "create table if not exists DATABASELINK(id INTEGER PRIMARY KEY AUTOINCREMENT,type varchar(20),"
                        + "name text ,driver varchar(100),ip varchar(30),port varchar(10),"
                        + "dataName varchar(40),username varchar(40),userpassword varchar(40) ,"
                        + "webuser varchar(40),edition  varchar(300), UNIQUE(name));");
    }
}
