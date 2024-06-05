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
 *  ConnectionDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/ConnectionDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.sql.Connection;
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
    //private Connection connection;

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
