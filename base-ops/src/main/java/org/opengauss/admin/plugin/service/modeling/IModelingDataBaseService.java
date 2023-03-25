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
 * IModelingDataBaseService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/IModelingDataBaseService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.domain.model.modeling.OpenGaussConnectorBody;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
* @author LZW
* @description ops facade and db operation
* @createDate 2022-08-10 11:41:13
*/
public interface IModelingDataBaseService {

    List<Map<String, Object>> executeWithClusterNode(OpsClusterNodeVO clusterNode, String sql, List<String> params) throws SQLException, ClassNotFoundException;

    List<Map<String, Object>> executeWithClusterNodeAndSchema(OpsClusterNodeVO clusterNode, String schema, String sql, List<String> params) throws SQLException, ClassNotFoundException;

    List<Map<String, Object>> queryWithSqlObject(ModelingDataFlowSqlObject sqlObject) throws SQLException, ClassNotFoundException;

    String updateWithSqlObject(ModelingDataFlowSqlObject sqlObject, String queryType) throws SQLException, ClassNotFoundException;

    Object getSqlResult(ModelingDataFlowSqlObject sqlObject);

    List<OpsClusterVO> getClusterList();

    List<OpsClusterVO> getClusterListWithDataBaseName();

    OpsClusterNodeVO getClusterNodeById(String clusterNodeId);

    ResultSet query(OpenGaussConnectorBody openGaussConnectorBody, String sql, List<String> params) throws ClassNotFoundException, SQLException;

    String update(OpenGaussConnectorBody openGaussConnectorBody, String sql, String queryType) throws ClassNotFoundException, SQLException;

    List<Map<String, Object>> convertList(ResultSet rs);

}
