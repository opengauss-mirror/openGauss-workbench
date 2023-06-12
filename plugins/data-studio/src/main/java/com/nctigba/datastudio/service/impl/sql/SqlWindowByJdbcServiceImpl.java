/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.service.SqlWindowByJdbcService;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;

@Service
public class SqlWindowByJdbcServiceImpl implements SqlWindowByJdbcService {
    @Override
    public List<String> schemaListQuerySQL(String jdbcUrl, String username, String password, String sql) {
        List<String> list = new ArrayList<>();
        try (
                Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password);
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("schema_name"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public DataListDTO dataListQuerySQL(String jdbcUrl, String username, String password, String tableSql,
                                        String viewSql, String fun_prosSql, String schema_name) {
        DataListDTO dataList = new DataListDTO();
        List<Map<String, String>> table = new ArrayList<>();
        List<Map<String, String>> view = new ArrayList<>();
        List<Map<String, String>> fun_pro = new ArrayList<>();
        try (
                Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password);
                PreparedStatement tableValue = connection.prepareStatement(tableSql);
                PreparedStatement viewValue = connection.prepareStatement(viewSql);
                PreparedStatement fun_proValue = connection.prepareStatement(fun_prosSql);
                ResultSet rs1 = tableValue.executeQuery();
                ResultSet rs2 = viewValue.executeQuery();
                ResultSet rs3 = fun_proValue.executeQuery()
        ) {
            while (rs1.next()) {
                Map<String, String> map = new HashMap();
                map.put(OID, rs1.getString(OID));
                map.put(NAME, rs1.getString("tablename"));
                table.add(map);
            }
            while (rs2.next()) {
                Map<String, String> map = new HashMap();
                map.put(OID, rs2.getString(OID));
                map.put(NAME, rs2.getString("viewname"));
                view.add(map);
            }
            while (rs3.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, rs3.getString(OID));
                map.put(NAME, rs3.getString("proname"));
                fun_pro.add(map);
            }

            dataList.setSchema_name(schema_name);
            dataList.setTable(table);
            dataList.setView(view);
            dataList.setFun_pro(fun_pro);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dataList;
    }
}
