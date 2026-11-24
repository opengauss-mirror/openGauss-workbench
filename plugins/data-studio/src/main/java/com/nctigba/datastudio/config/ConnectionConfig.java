/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.datastudio.config;

import com.nctigba.datastudio.base.JdbcConnectionManager;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.utils.ConnectionUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

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
        Connection connection = JdbcConnectionManager.getConnection(
                connectionDTO.getUrl(),
                connectionDTO.getDbUser(),
                encryptionUtils.decrypt(connectionDTO.getDbPassword()));
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
        return connection;
    }
}
