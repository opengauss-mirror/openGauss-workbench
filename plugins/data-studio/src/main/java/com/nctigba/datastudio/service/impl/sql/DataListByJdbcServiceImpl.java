/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.util.ConnectionUtils;
import com.nctigba.datastudio.util.DebugUtils;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PARTTYPE;
import static com.nctigba.datastudio.constants.CommonConstants.PKG_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_PACKAGE_ID;
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
            String schema_name) throws SQLException, InterruptedException {
        DataListDTO dataList = new DataListDTO();
        List<Map<String, String>> table = new ArrayList<>();
        List<Map<String, String>> view = new ArrayList<>();
        List<Map<String, String>> sequence = new ArrayList<>();
        List<Map<String, String>> synonym = new ArrayList<>();
        List<Map<String, Object>> fun_pro = new ArrayList<>();
        Map<String, String> funTypeMap = new HashMap<>();
        try (
                Connection connection = ConnectionUtils.connectGet(jdbcUrl, username, password);
        ) {
            CountDownLatch countDownLatch = new CountDownLatch(5);
            ThreadUtil.execute(() -> {
                try (
                        PreparedStatement tableValue = connection.prepareStatement(tableSql);
                        ResultSet rs1 = tableValue.executeQuery();) {
                    while (rs1.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(OID, rs1.getString(OID));
                        map.put(NAME, rs1.getString("tablename"));
                        map.put(PARTTYPE, rs1.getString(PARTTYPE));
                        table.add(map);
                    }
                    dataList.setTable(table);
                } catch (SQLException e) {
                    throw new CustomException(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
            ThreadUtil.execute(() -> {
                try (PreparedStatement viewValue = connection.prepareStatement(viewSql);
                     ResultSet rs2 = viewValue.executeQuery();) {
                    while (rs2.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(OID, rs2.getString(OID));
                        map.put(NAME, rs2.getString("viewname"));
                        view.add(map);
                    }
                    dataList.setView(view);
                } catch (SQLException e) {
                    throw new CustomException(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
            ThreadUtil.execute(() -> {
                try (
                        PreparedStatement fun_type = connection.prepareStatement(GET_TYPENAME_SQL);
                        PreparedStatement fun_proValue = connection.prepareStatement(fun_prosSql);
                        ResultSet rs3 = fun_proValue.executeQuery();
                        ResultSet rs4 = fun_type.executeQuery();) {
                    while (rs4.next()) {
                        funTypeMap.put(rs4.getString(OID), rs4.getString("typname"));
                    }
                    Map<String, List<Map<String, String>>> packageMap = new HashMap<>();
                    while (rs3.next()) {
                        String proArgTypes = rs3.getString(PRO_ARG_TYPES);
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
                        String proPackageId = rs3.getString(PRO_PACKAGE_ID);
                        String oid = rs3.getString(OID);
                        String proName = DebugUtils.needQuoteName(rs3.getString(PRO_NAME));
                        Map<String, Object> map = new HashMap<>();
                        if ("0".equals(proPackageId)) {
                            map.put(OID, oid);
                            map.put(NAME, proName + "(" + asd + ")");
                            map.put("isPackage", false);
                            fun_pro.add(map);
                        } else {
                            Map<String, String> childrenMap = new HashMap<>();
                            childrenMap.put(OID, oid);
                            childrenMap.put(NAME, proName);
                            childrenMap.put(PRO_PACKAGE_ID, proPackageId);
                            childrenMap.put(PKG_NAME, rs3.getString(PKG_NAME));
                            if (packageMap.containsKey(proPackageId)) {
                                List<Map<String, String>> childrenList = packageMap.get(proPackageId);
                                childrenList.add(childrenMap);
                            } else {
                                List<Map<String, String>> childrenList = new ArrayList<>();
                                childrenList.add(childrenMap);
                                packageMap.put(proPackageId, childrenList);
                            }
                        }
                    }
                    for (String key : packageMap.keySet()) {
                        List<Map<String, String>> childrenList = packageMap.get(key);
                        Map<String, Object> map = new HashMap<>();
                        map.put(OID, childrenList.get(0).get(PRO_PACKAGE_ID));
                        map.put(NAME, childrenList.get(0).get(PKG_NAME));
                        map.put("isPackage", true);
                        map.put("children", childrenList);
                        fun_pro.add(map);
                    }
                    dataList.setFun_pro(fun_pro);
                } catch (SQLException e) {
                    throw new CustomException(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
            ThreadUtil.execute(() -> {
                try (PreparedStatement sequenceValue = connection.prepareStatement(sequenceSql);
                     ResultSet rs5 = sequenceValue.executeQuery();) {
                    while (rs5.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(OID, rs5.getString(OID));
                        map.put(NAME, rs5.getString("relname"));
                        sequence.add(map);
                    }
                    dataList.setSequence(sequence);
                } catch (SQLException e) {
                    throw new CustomException(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
            ThreadUtil.execute(() -> {
                try (PreparedStatement synonymValue = connection.prepareStatement(synonymSql);
                     ResultSet rs6 = synonymValue.executeQuery()) {
                    while (rs6.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(OID, rs6.getString(OID));
                        map.put(NAME, rs6.getString("synname"));
                        synonym.add(map);
                    }
                    dataList.setSynonym(synonym);
                } catch (SQLException e) {
                    throw new CustomException(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });

            dataList.setSchema_name(schema_name);
            countDownLatch.await(5, TimeUnit.MINUTES);
        }
        return dataList;
    }

}
