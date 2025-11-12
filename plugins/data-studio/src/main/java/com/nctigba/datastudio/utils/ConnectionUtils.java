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
 *  ConnectionUtils.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/utils/ConnectionUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.utils;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;

import com.nctigba.datastudio.base.SpringApplicationContext;

import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ConnectionUtils
 *
 * @since 2023-6-26
 */
public class ConnectionUtils {
    /**
     * get connect
     *
     * @param jdbcUrl  jdbcUrl
     * @param username username
     * @param password password
     * @return Connection
     * @throws SQLException SQLException
     */
    public static Connection connectGet(String jdbcUrl, String username, String password)
            throws SQLException {
        MainApplicationContext mainContext = SpringApplicationContext.getApplicationContext().getBean(
            MainApplicationContext.class);
        SpringBeanFactory springBeanFactory = mainContext.getSpringBeanFactory();
        EncryptionUtils encryptionUtils = springBeanFactory.getBean(EncryptionUtils.class);
        return DriverManager.getConnection(jdbcUrl, username, encryptionUtils.decrypt(password));
    }
}
