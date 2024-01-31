/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.utils.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * FileLoader
 *
 * @author liu
 * @since 2023-01-03
 */
@Slf4j
public class JdbcUtil {
    /**
     * getAllSchemas
     *
     * @param conn conn
     * @param database database
     * @return List<String>
     */
    public static List<String> getAllSchemas(Connection conn, String database) {
        List<String> schemaList = new ArrayList<>();
        try {
            String sql = "SELECT schema_name FROM information_schema.schemata WHERE catalog_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, database);
            ResultSet rs = pstmt.executeQuery();
            // 输出查询结果
            while (rs.next()) {
                String schemaName = rs.getString(1);
                schemaList.add(schemaName.trim());
            }
            // 关闭连接
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            log.error("JdbcUtil occur error {}", e.getMessage());
        }
        return schemaList;
    }
}
