/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PARTTYPE;
import static com.nctigba.datastudio.constants.CommonConstants.SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.GET_TYPENAME_SQL;

/**
 * DataListByJdbcServiceImpl
 *
 * @since 2023-6-26
 */
@Service
public class DataListByJdbcServiceImpl implements DataListByJdbcService {

    @Override
    public DataListDTO dataListQuerySQL(
            String jdbcUrl, String username, String password, String tableSql,
            String viewSql, String fun_prosSql, String sequenceSql, String synonymSql,
            String schema_name) throws SQLException {
        DataListDTO dataList = new DataListDTO();
        List<Map<String, String>> table = new ArrayList<>();
        List<Map<String, String>> view = new ArrayList<>();
        List<Map<String, String>> sequence = new ArrayList<>();
        List<Map<String, String>> synonym = new ArrayList<>();
        List<Map<String, String>> fun_pro = new ArrayList<>();
        Map<String, String> funTypeMap = new HashMap<>();
        try (
                Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password);
                PreparedStatement tableValue = connection.prepareStatement(tableSql);
                PreparedStatement viewValue = connection.prepareStatement(viewSql);
                PreparedStatement fun_proValue = connection.prepareStatement(fun_prosSql);
                PreparedStatement sequenceValue = connection.prepareStatement(sequenceSql);
                PreparedStatement synonymValue = connection.prepareStatement(synonymSql);
                PreparedStatement fun_type = connection.prepareStatement(GET_TYPENAME_SQL);
                ResultSet rs1 = tableValue.executeQuery();
                ResultSet rs2 = viewValue.executeQuery();
                ResultSet rs3 = fun_proValue.executeQuery();
                ResultSet rs4 = fun_type.executeQuery();
                ResultSet rs5 = sequenceValue.executeQuery();
                ResultSet rs6 = synonymValue.executeQuery()
        ) {
            while (rs1.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, rs1.getString(OID));
                map.put(NAME, rs1.getString("tablename"));
                map.put(PARTTYPE, rs1.getString("parttype"));
                table.add(map);
            }
            while (rs2.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, rs2.getString(OID));
                map.put(NAME, rs2.getString("viewname"));
                view.add(map);
            }
            while (rs4.next()) {
                funTypeMap.put(rs4.getString(OID), rs4.getString("typname"));
            }
            while (rs3.next()) {
                String proArgTypes = rs3.getString("proargtypes");
                String[] splited = proArgTypes.split(SPACE);
                StringBuilder asd = new StringBuilder();
                for (int i = 0; i < splited.length; i++) {
                    if (!StringUtils.isEmpty(splited[i])) {
                            asd.append(ParamTypeEnum.parseType(funTypeMap.get(splited[i])));
                        if (splited.length - 1 != i) {
                            asd.append(",");
                        }
                    }
                }
                Map<String, String> map = new HashMap<>();
                map.put(OID, rs3.getString(OID));
                map.put(NAME, rs3.getString("proname") + "(" + asd + ")");
                fun_pro.add(map);
            }
            while (rs5.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, rs5.getString(OID));
                map.put(NAME, rs5.getString("relname"));
                sequence.add(map);
            }
            while (rs6.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, rs6.getString(OID));
                map.put(NAME, rs6.getString("synname"));
                synonym.add(map);
            }
            dataList.setSchema_name(schema_name);
            dataList.setTable(table);
            dataList.setView(view);
            dataList.setSequence(sequence);
            dataList.setSynonym(synonym);
            dataList.setFun_pro(fun_pro);
        }
        return dataList;
    }

}
