package com.tools.monitor.util.jdbc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.tools.monitor.entity.DataSource;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.entity.zabbix.WholeIds;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * JdbcUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class JdbcUtil {
    public static List<JSONObject> executeSql(DataSource datasource, String sql) {
        return executeSql(datasource, sql, new ArrayList<Object>());
    }

    public static List<JSONObject> executeSql(DataSource datasource, String sql, List<Object> jdbcParamValues) {
        DruidPooledConnection connection = null;
        try {
            connection = PoolManager.getPooledConnection(datasource);
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 1; i <= jdbcParamValues.size(); i++) {
                statement.setObject(i, jdbcParamValues.get(i - 1));
            }
            boolean hasResultSet = statement.execute();
            if (hasResultSet) {
                ResultSet rs = statement.getResultSet();
                int columnCount = rs.getMetaData().getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnLabel(i);
                    columns.add(columnName);
                }
                List<JSONObject> list = new ArrayList<>();
                while (rs.next()) {
                    JSONObject jo = new JSONObject();
                    columns.stream().forEach(t -> {
                        try {
                            Object value = rs.getObject(t);
                            jo.put(t, value);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    list.add(jo);
                }
                return list;
            } else {
                int updateCount = statement.getUpdateCount();
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("executeSql-->{}", e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("executeSql_connection-->{}", e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    public static void batchExecuteSql(DataSource datasource, List<String> sqlList,String name,String hoatName) {
        DruidPooledConnection connection = null;
        try {
            connection = PoolManager.getPooledConnection(datasource);
            Statement statement = connection.createStatement();
            connection.setAutoCommit(false);
            if (CollectionUtil.isNotEmpty(sqlList)) {
                for (String str : sqlList) {
                    statement.addBatch(str);
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            log.error("batchExecuteSql__>{}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("batchExecuteSqle-->{}", e.getMessage());
                }
            }
        }
    }


    public static Integer getResulte(String key, List<JSONObject> js) {
        if (CollectionUtil.isNotEmpty(js)) {
            return js.get(0).getInteger(key);
        }
        return 0;
    }

    public static DataSource getDataSource(SysConfig sysConfig) {
        DataSource dataSource = new DataSource();
        dataSource.setId(UUID.randomUUID().toString(true));
        dataSource.setUrl(sysConfig.getUrl());
        dataSource.setUsername(sysConfig.getUserName());
        dataSource.setPassword(sysConfig.getPassword());
        dataSource.setDriver(sysConfig.getDriver());
        dataSource.setName(sysConfig.getConnectName());
        return dataSource;
    }

    public static WholeIds getAllId(List<JSONObject> jsonObjects) {
        WholeIds wholeIds = new WholeIds();
        if (CollectionUtil.isNotEmpty(jsonObjects)) {
            for (JSONObject jsonObject : jsonObjects) {
                if (jsonObject.get("field_name").equals("hostid")) {
                    wholeIds.setHostid(jsonObject.getInteger("nextid"));
                }
                if (jsonObject.get("field_name").equals("interfaceid")) {
                    wholeIds.setInterfaceid(jsonObject.getInteger("nextid"));
                }
                if (jsonObject.get("field_name").equals("itemid")) {
                    wholeIds.setItemid(jsonObject.getInteger("nextid"));
                }
                if (jsonObject.get("field_name").equals("item_preprocid")) {
                    wholeIds.setItem_preprocid(jsonObject.getInteger("nextid"));
                }
            }
        }
        dealWholeIds(wholeIds);
        return wholeIds;
    }

    private static void dealWholeIds(WholeIds wholeIds) {
        if (ObjectUtil.isEmpty(wholeIds.getHostid())) {
            wholeIds.setHostid(0);
        }
        if (ObjectUtil.isEmpty(wholeIds.getInterfaceid())) {
            wholeIds.setInterfaceid(0);
        }
        if (ObjectUtil.isEmpty(wholeIds.getItem_preprocid())) {
            wholeIds.setItem_preprocid(0);
        }
        if (ObjectUtil.isEmpty(wholeIds.getItemid())) {
            wholeIds.setItemid(0);
        }
    }
}
