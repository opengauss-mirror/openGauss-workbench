package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.GET_TYPENAME_SQL;

@Service
public class DataListByJdbcServiceImpl implements DataListByJdbcService {
    @Override
    public List<String> schemaListQuerySQL(String jdbcUrl, String username, String password, String sql) {
        List<String> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password)) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("schema_name"));
            }

        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        return list;
    }

    @Override
    public DataListDTO dataListQuerySQL(String jdbcUrl, String username, String password, String tableSql, String viewSql, String fun_prosSql, String sequenceSql, String synonymSql, String schema_name) {
        DataListDTO dataList = new DataListDTO();
        List<String> table = new ArrayList<>();
        List<String> view = new ArrayList<>();
        List<String> sequence = new ArrayList<>();
        List<String> synonym = new ArrayList<>();
        List<String> fun_pro = new ArrayList<>();
        Map funTypeMap = new HashMap<>();
        try (Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password)) {
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
            ResultSet rs6 = synonymValue.executeQuery();
            while (rs1.next()) {
                table.add(rs1.getString("tablename"));
            }
            while (rs2.next()) {
                view.add(rs2.getString("viewname"));
            }
            while (rs4.next()) {
                funTypeMap.put(rs4.getString("oid"), rs4.getString("typname"));
            }
            while (rs3.next()) {
                String proArgTypes = rs3.getString("proargtypes");
                var splited = proArgTypes.split(SPACE);
                String asd = "";
                for (int i = 0; i < splited.length; i++) {
                    if (!StringUtils.isEmpty(splited[i])) {
                        try {
                            asd += ParamTypeEnum.parseType((String) funTypeMap.get(splited[i]));
                        } catch (Exception e) {
                            asd += funTypeMap.get(splited[i]);
                        }
                        if (splited.length - 1 != i) {
                            asd += ",";
                        }
                    }
                }
                fun_pro.add(rs3.getString("proname") + "(" + asd + ")");
            }
            while (rs5.next()) {
                sequence.add(rs5.getString("relname"));
            }
            while (rs6.next()) {
                synonym.add(rs6.getString("synname"));
            }
            dataList.setSchema_name(schema_name);
            dataList.setTable(table);
            dataList.setView(view);
            dataList.setSequence(sequence);
            dataList.setSynonym(synonym);
            dataList.setFun_pro(fun_pro);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        return dataList;
    }
}
