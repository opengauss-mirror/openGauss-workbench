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
 *  DbConnectionCreateDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/DbConnectionCreateDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;


import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DbConnectionCreateDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DbConnectionCreateDTO {
    private String connectionid;
    private String id;
    private String type;
    private String name;
    private String driver;
    private String ip;
    private String port;
    private String dataName;
    private String userName;
    private String password;
    private String webUser;
    private String isRememberPassword;

    /**
     * set database connection
     *
     * @return DatabaseConnectionDO
     */
    public DatabaseConnectionDO toDatabaseConnection() {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        databaseConnectionDO.setId(id);
        databaseConnectionDO.setType(type);
        databaseConnectionDO.setName(name.trim());
        databaseConnectionDO.setDriver("");
        databaseConnectionDO.setIp(ip);
        databaseConnectionDO.setPort(port);
        databaseConnectionDO.setDataName(dataName);
        databaseConnectionDO.setUserName(userName);
        databaseConnectionDO.setPassword(password);
        databaseConnectionDO.setWebUser(webUser);
        return databaseConnectionDO;
    }
}
