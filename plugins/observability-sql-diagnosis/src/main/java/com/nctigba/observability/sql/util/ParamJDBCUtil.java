/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.ParamInfo;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ParamJDBCUtil
 *
 * @author luomeng
 * @since 2023/9/26
 */
@Component
public class ParamJDBCUtil {
    private static synchronized Connection connectSqlite() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(JDBC.PREFIX + "data/" + CommonConstants.PARAM_DATABASE_NAME + ".db");
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return conn;
    }

    /**
     * Get param info
     *
     * @param sql select sql
     * @return list
     */
    public List<ParamInfo> result(String sql) {
        List<ParamInfo> list = new ArrayList<>();
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                ParamInfo info = new ParamInfo(result.getInt(1),
                        ParamInfo.ParamType.valueOf(result.getString(2)),
                        result.getString(3), result.getString(4), result.getString(5),
                        result.getString(6), result.getString(7), result.getString(8),
                        result.getString(9));
                list.add(info);
            }
            return list;
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
    }

    /**
     * Get param count
     *
     * @param sql select sql
     * @return int
     */
    public int selectCount(String sql) {
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return 0;
    }
}
