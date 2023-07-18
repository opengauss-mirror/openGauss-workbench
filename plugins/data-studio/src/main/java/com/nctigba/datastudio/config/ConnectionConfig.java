/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.config;

import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.service.impl.sql.DbConnectionServiceImpl;
import com.nctigba.datastudio.util.ConnectionUtils;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * ConnectionConfig
 *
 * @since 2023-06-26
 */
@Service("connectionConfig")
public class ConnectionConfig {
    @Resource
    private DbConnectionServiceImpl dbConnectionServiceImpl;

    /**
     * connect database
     *
     * @param uuid uuid
     * @return Connection
     * @throws SQLException SQLException
     */
    public Connection connectDatabase(String uuid) throws SQLException {
        if (!conMap.containsKey(uuid)) {
            throw new CustomException(LocaleString.transLanguage("1004"));
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
            throw new CustomException(LocaleString.transLanguage("1004"));
        }
        ConnectionDTO connectionDTO = conMap.get(uuid);
        Connection connection = ConnectionUtils.connectGet(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                connectionDTO.getDbPassword());
        connectionDTO.updateConnectionDTO(connectionDTO, winName);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return connection;
    }
}
