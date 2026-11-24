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

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.utils.ConnectionUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MetaDataByJdbcServiceImpl
 *
 * @since 2023-6-26
 */
@Service
public class MetaDataByJdbcServiceImpl implements MetaDataByJdbcService {
    @Override
    public void testQuerySQL(String jdbcUrl, String userName, String password, String sql) {
        try (
                Connection connection = ConnectionUtils.connectGet(jdbcUrl, userName, password)
        ) {
            connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public String versionSQL(String jdbcUrl, String userName, String password, String sql) {
        try (
                Connection connection = ConnectionUtils.connectGet(jdbcUrl, userName, password);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {

            resultSet.next();
            String edition = resultSet.getNString(1);
            StringBuilder sb = new StringBuilder();
            boolean ba = false;
            for (char ch : edition.toCharArray()) {
                if (ch == ')') break;
                if (ba) sb.append(ch);
                if (ch == '(') {
                    ba = true;
                }
            }
            return sb.toString();
        } catch (SQLException e) {
            throw new CustomException(e.getMessage());
        }
    }
}
