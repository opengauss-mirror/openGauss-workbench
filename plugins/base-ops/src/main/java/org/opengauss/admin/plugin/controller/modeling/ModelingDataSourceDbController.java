/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.controller.modeling;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.service.modeling.IModelingDataBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@RestController
@RequestMapping("/modeling/dataflow/dataSourceDb")
public class ModelingDataSourceDbController extends BaseController {

    @Autowired
    private IModelingDataBaseService modelingDataSourceDbService;

    @GetMapping("/list")
    public AjaxResult getList() {
        List<OpsClusterVO> list = modelingDataSourceDbService.getClusterListWithDataBaseName();
        return AjaxResult.success(list);
    }

    @GetMapping("/getSchemaByClusterNodeId/{dbName}/{clusterNodeId}")
    public AjaxResult getSchemaByClusterNodeId(@PathVariable String dbName,@PathVariable String clusterNodeId) {
        List<OpsClusterVO> list = modelingDataSourceDbService.getClusterList();
        List<String> schemaList = new ArrayList<>();
        list.forEach(opsClusterVO -> {
            opsClusterVO.getClusterNodes().forEach(node->{
                if (Objects.equals(node.getNodeId(), clusterNodeId)) {
                    String querySchemaSql = "select * from information_schema.schemata where catalog_name = '" + dbName + "';";
                    try {
                        node.setDbName(dbName);
                        List<Map<String, Object>> resultSet = modelingDataSourceDbService.executeWithClusterNode(node,querySchemaSql,null);
                        resultSet.forEach(ret->{
                            schemaList.add((String) ret.get("schema_name"));
                        });
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
        return AjaxResult.success(schemaList);
    }

    @GetMapping(value = "/getTablesBySchema/{dbName}/{clusterNodeId}/{schema}")
    public AjaxResult getTablesBySchema(@PathVariable String dbName,@PathVariable String clusterNodeId, @PathVariable String schema) throws ClassNotFoundException, SQLException {
        String sql = "SELECT tablename FROM pg_tables " +
                "WHERE tablename NOT LIKE 'pg%' " +
                "AND tablename NOT LIKE 'gs%' " +
                "AND tablename NOT LIKE 'sql_%' " +
                "AND schemaname='"+schema+"' " +
                "ORDER BY tablename;";
        OpsClusterNodeVO node = modelingDataSourceDbService.getClusterNodeById(clusterNodeId);
        if (node == null) {
            return AjaxResult.success(new ArrayList<>());
        }

        node.setDbName(dbName);

        List<Map<String, Object>> resultSet = modelingDataSourceDbService.executeWithClusterNodeAndSchema(node,schema,sql,null);

        return AjaxResult.success(resultSet);
    }

    @GetMapping(value = "/getFieldsByTable/{dbName}/{clusterNodeId}/{schema}/{tableName}")
    public AjaxResult getFieldsByTable(@PathVariable String dbName,@PathVariable String clusterNodeId, @PathVariable String schema, @PathVariable String tableName) throws ClassNotFoundException, SQLException {
        String sql = "SELECT column_name AS name, data_type as type, is_nullable AS NOTNULL\n" +
                "FROM information_schema.columns\n" +
                "WHERE table_name='"+tableName+"' AND table_schema = '"+schema+"';";

        OpsClusterNodeVO node = modelingDataSourceDbService.getClusterNodeById(clusterNodeId);

        node.setDbName(dbName);

        List<Map<String, Object>> resultSet = modelingDataSourceDbService.executeWithClusterNodeAndSchema(node,schema,sql,null);
        return AjaxResult.success(resultSet);
    }

}

