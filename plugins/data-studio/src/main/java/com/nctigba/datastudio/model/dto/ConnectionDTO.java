/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;

@NoArgsConstructor
@Data
public class ConnectionDTO {
    private Date lastDate;
    private HashSet socketSet = new HashSet<>();
    private String url;
    private String dbUser;
    private String dbPassword;
    private String type;


    public void setConnectionDTO(DatabaseConnectionUrlDO databaseConnectionUrlDO) {
        Date date = new Date();
        this.lastDate = date;
        this.url = databaseConnectionUrlDO.getUrl();
        this.dbUser = databaseConnectionUrlDO.getUserName();
        this.dbPassword = databaseConnectionUrlDO.getPassword();
        this.type = databaseConnectionUrlDO.getType();
    }

    public void setIpConnectionDTO(DatabaseConnectionDO databaseConnectionDO) {
        Date date = new Date();
        this.lastDate = date;
        this.url = GET_URL_JDBC + databaseConnectionDO.getIp() + ":" + databaseConnectionDO.getPort() + "/" + databaseConnectionDO.getDataName() + CONFIGURE_TIME;
        this.dbUser = databaseConnectionDO.getUserName();
        this.dbPassword = databaseConnectionDO.getPassword();
        this.type = databaseConnectionDO.getType();
    }

    public void updataConnectionDTO(ConnectionDTO aon, String socketSet) {
        Date date = new Date();
        this.lastDate = date;
        this.socketSet.add(socketSet);
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
        this.type = aon.getType();
    }

    public void updataConnectionDTO(ConnectionDTO aon) {
        Date date = new Date();
        this.lastDate = date;
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
        this.type = aon.getType();
    }

    public void reduceConnectionDTO(ConnectionDTO aon, String socketSet) {
        Date date = new Date();
        this.lastDate = date;
        this.socketSet.remove(socketSet);
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
        this.type = aon.getType();
    }
}
