/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;

/**
 * ConnectionDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class ConnectionDTO {
    private Date lastDate;
    private HashSet<String> socketSet = new HashSet<>();
    private String url;
    private String dbUser;
    private String dbPassword;
    private String type;
    private Integer timeLength;

    /**
     * set connection dto
     *
     * @param databaseConnectionUrlDO databaseConnectionUrlDO
     */
    public void setConnectionDTO(DatabaseConnectionUrlDO databaseConnectionUrlDO) {
        this.lastDate = new Date();
        this.url = databaseConnectionUrlDO.getUrl();
        this.dbUser = databaseConnectionUrlDO.getUserName();
        this.dbPassword = databaseConnectionUrlDO.getPassword();
        this.type = databaseConnectionUrlDO.getType();
        this.timeLength = 2;
    }

    /**
     * set ip connection dto
     *
     * @param databaseConnectionDO databaseConnectionDO
     */
    public void setIpConnectionDTO(DatabaseConnectionDO databaseConnectionDO) {
        this.lastDate = new Date();
        this.url = GET_URL_JDBC + databaseConnectionDO.getIp() + ":" + databaseConnectionDO.getPort()
                + "/" + databaseConnectionDO.getDataName() + CONFIGURE_TIME;
        this.dbUser = databaseConnectionDO.getUserName();
        this.dbPassword = databaseConnectionDO.getPassword();
        this.type = databaseConnectionDO.getType();
        this.timeLength = 2;
    }

    /**
     * update connection dto
     *
     * @param aon aon
     * @param socketSet socketSet
     */
    public void updateConnectionDTO(ConnectionDTO aon, String socketSet) {
        this.lastDate = new Date();
        this.socketSet.add(socketSet);
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
        this.type = aon.getType();
    }

    /**
     * update connection dto
     *
     * @param aon aon
     */
    public void updateConnectionDTO(ConnectionDTO aon) {
        this.lastDate = new Date();
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
        this.type = aon.getType();
        this.timeLength = aon.timeLength;
    }

    /**
     * reduce connection dto
     *
     * @param aon aon
     * @param socketSet socketSet
     */
    public void reduceConnectionDTO(ConnectionDTO aon, String socketSet) {
        this.lastDate = new Date();
        this.socketSet.remove(socketSet);
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
        this.type = aon.getType();
    }
}
