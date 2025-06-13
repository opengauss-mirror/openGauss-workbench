/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DatabaseConnectionDAO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/dao/DatabaseConnectionDAO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Long count = jdbcTemplate.queryForObject(
                GET_DATABASELINK_COUNT_SQL + " webuser = '" + webUser + "';", Long.class);
        if (count != 0) {
            List<Map<String, Object>> dataList = jdbcTemplate.queryForList(
                    GET_DATA_CONNECTION_NOT_P_SQL + " webuser = '" + webUser + "';");
            return dataList.stream().map(DatabaseConnectionDAO::getDatabaseConnectionDO)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @NotNull
    private static DatabaseConnectionDO getDatabaseConnectionDO(Map<String, Object> data) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        databaseConnectionDO.setId(String.valueOf(data.get("id")));
        if (data.get("type") instanceof String) {
            databaseConnectionDO.setType((String) data.get("type"));
        }
        if (data.get("name") instanceof String) {
            databaseConnectionDO.setName((String) data.get("name"));
        }
        if (data.get("driver") instanceof String) {
            databaseConnectionDO.setDriver((String) data.get("driver"));
        }
        if (data.get("ip") instanceof String) {
            databaseConnectionDO.setIp((String) data.get("ip"));
        }
        if (data.get("port") instanceof String) {
            databaseConnectionDO.setPort((String) data.get("port"));
        }
        if (data.get("dataname") instanceof String) {
            databaseConnectionDO.setDataName((String) data.get("dataname"));
        }
        if (data.get("username") instanceof String) {
            databaseConnectionDO.setUserName((String) data.get("username"));
        }
        if (data.get("userpassword") instanceof String) {
            databaseConnectionDO.setPassword((String) data.get("userpassword"));
        }
        if (data.get("edition") instanceof String) {
            databaseConnectionDO.setEdition((String) data.get("edition"));
        }
        if (data.get("webuser") instanceof String) {
            databaseConnectionDO.setWebUser((String) data.get("webuser"));
        }
        return databaseConnectionDO;
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
    public Long getJudgeName(String name, String webUser) {
        return jdbcTemplate.queryForObject(
                GET_DATABASELINK_COUNT_SQL + " name ='" + name + "' and webUser = '" + webUser + "'", Long.class);
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
                        + "dataName varchar(40),username varchar(40),userpassword varchar(128) ,"
                        + "webuser varchar(40),edition  varchar(300), UNIQUE(name));");
        // update table column
        Integer bytes = jdbcTemplate.queryForObject(
                "select BYTES from \"SYS_TABLES\" t left join \"SYS_COLUMNS\" c on t.id=c.\"TABLE#\" where t"
                        + ".name='databaselink' and c.name='userpassword';", Integer.class);
        if (bytes != null && bytes < 128) {
            jdbcTemplate.execute("alter table DATABASELINK alter column userpassword type varchar(128);");
        }
    }
}
