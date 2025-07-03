/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.enums;

import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConnectionContainer
 *
 * @author: wangchao
 * @Date: 2025/5/17 09:22
 * @Description: ConnectionContainer
 * @since 7.0.0-RC2
 **/
public interface ConnectionContainer {
    /**
     * configureDataSource
     *
     * @param hikariConfig hikariConfig
     * @param url url
     * @param user user
     * @param password password
     */
    void configureDataSource(HikariConfig hikariConfig, String url, String user, String password);

    /**
     * getConnection
     *
     * @param url url
     * @param user user
     * @param password password
     * @return Connection
     * @throws SQLException SQLException
     */
    Connection getConnection(String url, String user, String password) throws SQLException;
}
