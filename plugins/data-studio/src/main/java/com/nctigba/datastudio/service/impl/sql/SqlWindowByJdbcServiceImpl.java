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
import java.util.List;

@Service
public class SqlWindowByJdbcServiceImpl implements SqlWindowByJdbcService {
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
    public DataListDTO dataListQuerySQL(String jdbcUrl, String username, String password, String tableSql, String viewSql, String fun_prosSql, String schema_name) {
        DataListDTO dataList = new DataListDTO();
        List<String> table = new ArrayList<>();
        List<String> view = new ArrayList<>();
        List<String> fun_pro = new ArrayList<>();
        try (Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password)) {
            PreparedStatement tableValue = connection.prepareStatement(tableSql);
            PreparedStatement viewValue = connection.prepareStatement(viewSql);
            PreparedStatement fun_proValue = connection.prepareStatement(fun_prosSql);

            ResultSet rs1 = tableValue.executeQuery();
            ResultSet rs2 = viewValue.executeQuery();
            ResultSet rs3 = fun_proValue.executeQuery();
            while (rs1.next()) {
                table.add(rs1.getString("tablename"));
            }
            while (rs2.next()) {
                view.add(rs2.getString("viewname"));
            }
            while (rs3.next()) {
                fun_pro.add(rs3.getString("proname"));
            }

            dataList.setSchema_name(schema_name);
            dataList.setTable(table);
            dataList.setView(view);
            dataList.setFun_pro(fun_pro);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        return dataList;
    }
}
