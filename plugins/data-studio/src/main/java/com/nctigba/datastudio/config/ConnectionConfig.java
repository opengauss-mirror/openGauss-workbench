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
 *  ConnectionConfig.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/config/ConnectionConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.config;

import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.utils.ConnectionUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * ConnectionConfig
 *
 * @since 2023-06-26
 */
@Slf4j
@Service("connectionConfig")
public class ConnectionConfig {
    /**
     * connect database
     *
     * @param uuid uuid
     * @return Connection
     * @throws SQLException SQLException
     */
    public Connection connectDatabase(String uuid) throws SQLException {
        if (!conMap.containsKey(uuid)) {
            throw new CustomException(LocaleStringUtils.transLanguage("1004"), 555);
        }
        ConnectionDTO connectionDTO = conMap.get(uuid);
        Connection connection = ConnectionUtils.connectGet(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                connectionDTO.getDbPassword());
        connectionDTO.updateConnectionDTO(connectionDTO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return connection;
    }

    /**
     * connect database map
     *
     * @param uuid    uuid
     * @param winName winName
     * @return Connection
     * @throws SQLException SQLException
     */
    public Connection connectDatabaseMap(String uuid, String winName) throws SQLException {
        if (!conMap.containsKey(uuid)) {
            throw new CustomException(LocaleStringUtils.transLanguage("1004"), 555);
        }
        ConnectionDTO connectionDTO = conMap.get(uuid);
        Connection connection = ConnectionUtils.connectGet(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                connectionDTO.getDbPassword());
        connectionDTO.updateConnectionDTO(connectionDTO, winName);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        log.error("connectionDTO SQLException is: {}", connectionDTO);
        return connection;
    }
}
