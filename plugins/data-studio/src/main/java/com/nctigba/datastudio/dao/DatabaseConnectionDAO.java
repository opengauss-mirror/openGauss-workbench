package com.nctigba.datastudio.dao;


import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
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
import static com.nctigba.datastudio.constants.SqlConstants.GET_DATA_Connection_NOT_PASSWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_DATA_Connection_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;

@Repository
public class DatabaseConnectionDAO implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertTable(DatabaseConnectionDO databaseConnectionDO) {
        jdbcTemplate.execute("insert into DATABASELINK(type,name,driver,ip,port,dataname,username,userpassword,webuser) values('" + databaseConnectionDO.getType() + "','" + databaseConnectionDO.getName() + "','" + databaseConnectionDO.getDriver() + "','" + databaseConnectionDO.getIp() + "','" + databaseConnectionDO.getPort() + "','" + databaseConnectionDO.getDataName() + "','" + databaseConnectionDO.getUserName() + "','" + databaseConnectionDO.getPassword() + "','" + databaseConnectionDO.getWebUser() + "');");
    }

    public void updateTable(DatabaseConnectionDO databaseConnectionDO) {
        jdbcTemplate.execute("UPDATE DATABASELINK SET type= '" + databaseConnectionDO.getType() + "' ,driver = '" + databaseConnectionDO.getDriver() + "',ip= '" + databaseConnectionDO.getIp() + "' ,port = '" + databaseConnectionDO.getPort() + "',dataName = '" + databaseConnectionDO.getDataName() + "',username ='" + databaseConnectionDO.getUserName() + "' ,userpassword ='" + databaseConnectionDO.getPassword() + "'  WHERE name = '" + databaseConnectionDO.getName() + "'  and webuser = '" + databaseConnectionDO.getWebUser() + "';");
    }

    public List<DatabaseConnectionDO> selectTable(String webUser) {
        List list = new ArrayList<>();
        Map<String, Object> count = jdbcTemplate.queryForMap(GET_DATABASELINK_COUNT_SQL + " webuser = '" + webUser + "';");
        if ((int) count.get("count") == 0) {
            return null;
        } else {
            list = jdbcTemplate.query(GET_DATA_Connection_NOT_PASSWORD_SQL + " webuser = '" + webUser + "';", new BeanPropertyRowMapper<DatabaseConnectionDO>(DatabaseConnectionDO.class));
        }
        return list;
    }

    public DatabaseConnectionUrlDO getByName(String name, String webUser) {
        DatabaseConnectionUrlDO databaseConnectionUrlDO = new DatabaseConnectionUrlDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(GET_DATA_Connection_SQL + " name = '" + name + "' and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        }  else {
            databaseConnectionUrlDO.setId((Integer) data.get("id"));
            databaseConnectionUrlDO.setType((String) data.get("type"));
            databaseConnectionUrlDO.setName((String) data.get("name"));
            databaseConnectionUrlDO.setDriver((String) data.get("driver"));
            databaseConnectionUrlDO.setUrl(GET_URL_JDBC + data.get("ip") + ":" + data.get("port") + "/" + data.get("dataName") + CONFIGURE_TIME);
            databaseConnectionUrlDO.setUserName((String) data.get("username"));
            databaseConnectionUrlDO.setPassword((String) data.get("userpassword"));
            databaseConnectionUrlDO.setWebUser((String) data.get("webuser"));
            return databaseConnectionUrlDO;

        }
    }

    public Integer getJudgeName(String name, String webUser) {
        Map<String, Object> count = jdbcTemplate.queryForMap(GET_DATABASELINK_COUNT_SQL + " name ='" + name + "' and webUser = '" + webUser + "'");
        Integer a = (int) count.get("count");
        return a;
    }

    public void deleteTable(Integer id) {
        jdbcTemplate.execute("delete from DATABASELINK where id = " + id + ";");
    }


    public DatabaseConnectionDO getByIdDatabase(Integer id, String webUser) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(GET_DATA_Connection_SQL + " id =" + id + " and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        }  else {
            databaseConnectionDO.setId(String.valueOf(data.get("id")));
            databaseConnectionDO.setType((String) data.get("type"));
            databaseConnectionDO.setName((String) data.get("name"));
            databaseConnectionDO.setDriver((String) data.get("driver"));
            databaseConnectionDO.setIp((String) data.get("ip"));
            databaseConnectionDO.setPort((String) data.get("port"));
            databaseConnectionDO.setUserName((String) data.get("username"));
            databaseConnectionDO.setPassword((String) data.get("userpassword"));
            databaseConnectionDO.setWebUser((String) data.get("webuser"));
            return databaseConnectionDO;

        }
    }

    public DatabaseConnectionDO getAttributeById(String id, String webUser) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(GET_DATA_Connection_SQL + " id =" + id + " and webUser = '" + webUser + "';");
        if (StringUtils.isBlank(String.valueOf(data.get("id")))) {
            return null;
        }  else {
            databaseConnectionDO.setId(String.valueOf(data.get("id")));
            databaseConnectionDO.setType((String) data.get("type"));
            databaseConnectionDO.setName((String) data.get("name"));
            databaseConnectionDO.setDriver((String) data.get("driver"));
            databaseConnectionDO.setIp((String) data.get("ip"));
            databaseConnectionDO.setPort((String) data.get("port"));
            databaseConnectionDO.setDataName((String) data.get("dataname"));
            databaseConnectionDO.setUserName((String) data.get("username"));
            databaseConnectionDO.setWebUser((String) data.get("webuser"));
            return databaseConnectionDO;

        }
    }

    public DatabaseConnectionDO getAttributeByName(String name, String webUser) {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        Map<String, Object> data = jdbcTemplate.queryForMap(GET_DATA_Connection_SQL + " name ='" + name + "' and webUser = '" + webUser + "';");
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
            return databaseConnectionDO;

        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jdbcTemplate.execute("create table if not exists DATABASELINK(id INTEGER PRIMARY KEY,type varchar(20),name varchar(20),driver varchar(100),ip varchar(50),port varchar(20),dataName varchar(40),username varchar(40),userpassword varchar(40) ,webuser varchar(40), UNIQUE(name));");
    }
}
