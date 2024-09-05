/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * ModelingDataBaseServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/impl/ModelingDataBaseServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.impl;

import com.alibaba.fastjson.JSONArray;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.domain.model.modeling.OpenGaussConnectorBody;

import org.opengauss.admin.plugin.service.modeling.IModelingDataBaseService;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
* @author LZW
* @description ops facade
* @createDate 2022-08-10 11:41:13
*/
@Slf4j
@Service
public class ModelingDataBaseServiceImpl
    implements IModelingDataBaseService {

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Override
    public List<Map<String, Object>> executeWithClusterNode(OpsClusterNodeVO clusterNode, String sql, List<String> params) throws SQLException, ClassNotFoundException {
        OpenGaussConnectorBody openGaussConnectorBody = new OpenGaussConnectorBody(clusterNode);
        return queryToList(openGaussConnectorBody,sql,params);
    }

    @Override
    public List<Map<String, Object>> executeWithClusterNodeAndSchema(OpsClusterNodeVO clusterNode, String schema, String sql, List<String> params) throws SQLException, ClassNotFoundException {
        OpenGaussConnectorBody openGaussConnectorBody = new OpenGaussConnectorBody(clusterNode);
        openGaussConnectorBody.setSchema(schema);
        return queryToList(openGaussConnectorBody,sql,params);
    }

    @Override
    public List<Map<String, Object>> queryWithSqlObject(ModelingDataFlowSqlObject sqlObject) throws SQLException, ClassNotFoundException {
        assert sqlObject != null;

        OpenGaussConnectorBody openGaussConnectorBody = new OpenGaussConnectorBody(sqlObject.getExecuteClusterNode());
        openGaussConnectorBody.setSchema(sqlObject.getExecuteSchema());

        return queryToList(openGaussConnectorBody,sqlObject.getPreparedSql(),sqlObject.getPreparedParams());
    }

    @Override
    public String updateWithSqlObject(ModelingDataFlowSqlObject sqlObject, String queryType) throws SQLException, ClassNotFoundException {
        assert sqlObject != null;

        OpenGaussConnectorBody openGaussConnectorBody = new OpenGaussConnectorBody(sqlObject.getExecuteClusterNode());
        openGaussConnectorBody.setSchema(sqlObject.getExecuteSchema());

        return update(openGaussConnectorBody,sqlObject.toSql(),queryType);
    }

    @Override
    public Object getSqlResult(ModelingDataFlowSqlObject sqlObject) {
        try {
            if (Objects.equals(sqlObject.getMainType(), "query")) {
                return queryWithSqlObject(sqlObject);
            } else {
                return updateWithSqlObject(sqlObject,sqlObject.getMainType());
            }
        } catch (SQLException | ClassNotFoundException e) {
            return e.getMessage();
        }
    }

    @Override
    public List<OpsClusterVO> getClusterList() {
        if (opsFacade == null) {
            return null;
        }
        List<OpsClusterVO> list = opsFacade.listCluster();

        return list;
    }

    @Override
    public List<OpsClusterVO> getClusterListWithDataBaseName() {
        if (opsFacade == null) {
            return null;
        }
        List<OpsClusterVO> list = opsFacade.listCluster();
        list.forEach(cluster -> {
            cluster.getClusterNodes().forEach(node -> {
                //only support ssh default port
                if (node.getHostPort() == 22) {
                    //get database name list
                    String sql = "select datname from pg_database;";
                    try {
                        List<String> dbList = new ArrayList<>();

                        List<Map<String, Object>> resultSet = executeWithClusterNodeAndSchema(node,"",sql,null);
                        resultSet.forEach(ret->{
                            dbList.add((String) ret.get("datname"));
                        });
                        node.setDbName(JSONArray.toJSONString(dbList));
                    } catch (SQLException | ClassNotFoundException e) {
                        node.setDbName(JSONArray.toJSONString(new ArrayList<>()));
                    }
                } else {
                    node.setDbName(JSONArray.toJSONString(new ArrayList<>()));
                }
            });
        });
        return list;
    }

    @Override
    public OpsClusterNodeVO getClusterNodeById(String clusterNodeId) {
        if (opsFacade == null) {
            return null;
        }
        List<OpsClusterVO> list = opsFacade.listCluster();
        for (OpsClusterVO cluster : list) {
            for (OpsClusterNodeVO node : cluster.getClusterNodes()) {
                if (Objects.equals(node.getNodeId(), clusterNodeId)) {
                    return node;
                }
            }
        }
        return null;
    }

    public String update(OpenGaussConnectorBody openGaussConnectorBody, String sql, String queryType) throws ClassNotFoundException, SQLException
    {
        Connection conn = null;
        Class.forName("org.opengauss.Driver");
        String resultMessage;
        String openGaussUrl = "jdbc:opengauss://"+openGaussConnectorBody.getIp()+":"+openGaussConnectorBody.getPort()+"/"+openGaussConnectorBody.getDatabase()+"?currentSchema="+openGaussConnectorBody.getSchema();
        conn = DriverManager.getConnection(openGaussUrl, openGaussConnectorBody.getDbUser(),
            encryptionUtils.decrypt(openGaussConnectorBody.getDbPassword()));
        //start transaction
        conn.setAutoCommit(false);
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.executeUpdate();
            conn.commit();
            resultMessage = "database " + queryType + " success!";
        } catch (SQLException e) {
            conn.rollback();
            conn.close();
            resultMessage = "database " + queryType + " failed:" + e.getMessage();
        }

        conn.close();

        return resultMessage;
    }

    public ResultSet query(OpenGaussConnectorBody openGaussConnectorBody, String sql, List<String> params) throws ClassNotFoundException, SQLException
    {
        ResultSet resultSet;

        Class.forName("org.opengauss.Driver");
        String openGaussUrl = "jdbc:opengauss://"+openGaussConnectorBody.getIp()+":"+openGaussConnectorBody.getPort()+"/"+openGaussConnectorBody.getDatabase()+"?currentSchema="+openGaussConnectorBody.getSchema();
        Connection conn = DriverManager.getConnection(openGaussUrl, openGaussConnectorBody.getDbUser(),
            encryptionUtils.decrypt(openGaussConnectorBody.getDbPassword()));

        if (sql == null || conn == null) {
            throw new RuntimeException("sql is empty");
        }
        log.debug("prepared: " + sql);
        log.debug("params: " + params);

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        if (params != null && params.size() > 0) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setString(i + 1, params.get(i));
            }
        }

        log.debug("executing: " + preparedStatement.toString());

        resultSet = preparedStatement.executeQuery();
        conn.close();

        return resultSet;
    }

    public List<Map<String, Object>> queryToList(OpenGaussConnectorBody openGaussConnectorBody, String sql, List<String> params) throws ClassNotFoundException, SQLException
    {
        return convertList(this.query(openGaussConnectorBody,sql,params));
    }

    public List<Map<String, Object>> convertList(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}




