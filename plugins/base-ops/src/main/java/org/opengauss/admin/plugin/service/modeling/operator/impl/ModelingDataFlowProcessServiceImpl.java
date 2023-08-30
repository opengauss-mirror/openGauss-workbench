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
 * ModelingDataFlowProcessServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/ModelingDataFlowProcessServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONObject;


import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowLineParams;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowOperatorParams;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.IModelingDataBaseService;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowOperatorService;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowOperatorBuilderService;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowProcessService;
import org.opengauss.admin.plugin.service.modeling.operator.factory.ModelingDataFlowOperatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Administrator
 * @createDate 2022-08-10 10:00:51
 */
@Service
public class ModelingDataFlowProcessServiceImpl implements IModelingDataFlowProcessService {
    @Autowired
    ModelingDataFlowOperatorFactory modelingDataFlowOperatorFactory;
    @Autowired
    IModelingDataFlowOperatorService modelingDataFlowOperatorService;
    @Autowired
    private IModelingDataFlowService modelingDataFlowService;
    @Autowired
    private IModelingDataBaseService modelingDataBaseService;

    public ModelingDataFlowSqlObject initSqlObject(List<ModelingDataFlowOperatorParams> operatorParamsList)  {
        ModelingDataFlowSqlObject resultObj = new ModelingDataFlowSqlObject();

        for (ModelingDataFlowOperatorParams operatorParams : operatorParamsList) {
            String cellsType = operatorParams.getCellType();
            JSONObject data = operatorParams.getData();
            if (operatorParams.getDisable() == null || !operatorParams.getDisable()) {
                try {
                    IModelingDataFlowOperatorBuilderService builder = modelingDataFlowOperatorFactory.getBuilder(cellsType);
                    if (builder == null) {
                        throw new RuntimeException("unknown operator type :" + cellsType);
                    }
                    resultObj = builder.getOperatorSql(data,resultObj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            //config db and main operatorId
            fillStartOperator(resultObj, operatorParams);
        }

        return resultObj;
    }

    public ModelingDataFlowSqlObject initSqlObjectWithTargetOperatorId(List<ModelingDataFlowOperatorParams> operatorParamsList, String targetOperatorId) throws Exception {
        int targetLevel = -1;
        for (ModelingDataFlowOperatorParams operatorParams : operatorParamsList) {
            if (Objects.equals(operatorParams.getUid(), targetOperatorId)) {
                targetLevel = operatorParams.getLevel();
            }
        }
        if (targetLevel == -1) {
            return null;
        }

        ModelingDataFlowSqlObject resultObj = new ModelingDataFlowSqlObject();
        for (ModelingDataFlowOperatorParams operatorParams : operatorParamsList) {
            String cellsType = operatorParams.getCellType();
            JSONObject data = operatorParams.getData();
            if (operatorParams.getDisable() == null || !operatorParams.getDisable()) {
                resultObj = modelingDataFlowOperatorFactory.getBuilder(cellsType).getOperatorSql(data,resultObj);
            }
            //config db and main operatorId
            fillStartOperator(resultObj, operatorParams);
            //break if reach target operator
            if (operatorParams.getLevel() == targetLevel) {
                break;
            }
        }
        return resultObj;
    }

    @Override
    public List<ModelingDataFlowSqlObject> getAllSqlObject(JSONObject params) {
        ModelingDataFlowEntity dataFlow = modelingDataFlowService.getById(params.getString("dataFlowId"));

        List<JSONObject> cells = params.getJSONArray("cells").toJavaList(JSONObject.class);
        Map<String,List<ModelingDataFlowOperatorParams>> operatorParamsMap = dispatchOperatorsIntoMap(cells);

        List<ModelingDataFlowSqlObject> sqlObjects = new ArrayList<>();
        operatorParamsMap.forEach((k,v)->{
            ModelingDataFlowSqlObject sqlObject = initSqlObject(v);
            //check target db config and schema, fill with dataflow default db if null
            String targetDbName = sqlObject.getExecuteDbName() != null ? sqlObject.getExecuteDbName() : dataFlow.getDbName();
            String targetClusterNodeId = sqlObject.getExecuteClusterNodeId() != null ? sqlObject.getExecuteClusterNodeId() : dataFlow.getClusterNodeId();
            String targetSchema = sqlObject.getExecuteSchema() != null ? sqlObject.getExecuteSchema() : dataFlow.getSchema();
            OpsClusterNodeVO node = modelingDataBaseService.getClusterNodeById(targetClusterNodeId);
            node.setDbName(targetDbName);

            sqlObject.initDataBase(node,targetSchema);
            //prepare params @LZW 2022/11/24
            sqlObject.doPrepare();
            sqlObjects.add(sqlObject);
        });

        return sqlObjects;
    }

    @Override
    public ModelingDataFlowSqlObject getSqlObjectByTargetOperatorId(JSONObject params, String operatorId, String dataFlowId) {
        ModelingDataFlowEntity dataFlow = modelingDataFlowService.getById(dataFlowId);
        List<JSONObject> cells = params.getJSONArray("cells").toJavaList(JSONObject.class);
        Map<String,List<ModelingDataFlowOperatorParams>> operatorParamsMap = dispatchOperatorsIntoMap(cells);
        for(Map.Entry<String,List<ModelingDataFlowOperatorParams>> entry : operatorParamsMap.entrySet()){
            try {
                ModelingDataFlowSqlObject sqlObject = initSqlObjectWithTargetOperatorId(entry.getValue(),operatorId);
                //prepare params @LZW 2022/11/24
                if (sqlObject != null) {
                    //check target db config and schema, fill with dataflow default db if null
                    String targetDbName = sqlObject.getExecuteDbName() != null ? sqlObject.getExecuteDbName() : dataFlow.getDbName();
                    String targetClusterNodeId = sqlObject.getExecuteClusterNodeId() != null ? sqlObject.getExecuteClusterNodeId() : dataFlow.getClusterNodeId();
                    String targetSchema = sqlObject.getExecuteSchema() != null ? sqlObject.getExecuteSchema() : dataFlow.getSchema();
                    OpsClusterNodeVO node = modelingDataBaseService.getClusterNodeById(targetClusterNodeId);
                    node.setDbName(targetDbName);

                    sqlObject.initDataBase(node,targetSchema);
                    //prepare params @LZW 2022/11/24
                    sqlObject.doPrepare();
                    return sqlObject;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public ModelingDataFlowSqlObject getSqlByMainOperatorId(JSONObject params, String operatorId) {
        for (ModelingDataFlowSqlObject sqlObject : this.getAllSqlObject(params)) {
            if (Objects.equals(sqlObject.getMainOperatorId(), operatorId)) {
                return sqlObject;
            }
        }
        return null;
    }

    @Override
    public String getMainQueryType(JSONObject params) {
        for (ModelingDataFlowSqlObject sqlObject : this.getAllSqlObject(params)) {
            return sqlObject.getMainType();
        }
        return null;
    }

    @Override
    public Integer getQueryCount(JSONObject params) {
        return this.getAllSqlObject(params).size();
    }

    public Map<String,List<ModelingDataFlowOperatorParams>> dispatchOperatorsIntoMap(List<JSONObject> cells ) {

        //dispatch operators into seq
        Map<String,List<ModelingDataFlowOperatorParams>> result = new HashMap<>();
        //get enable type of operators from db config
        List<String> mainOperatorConfigNames = modelingDataFlowOperatorService.selectMainOperatorConfigName();
        //collect nodes and lines from params
        List<ModelingDataFlowOperatorParams> operatorParamsList = new ArrayList<>();
        List<ModelingDataFlowLineParams> lineList = new ArrayList<>();

        for(JSONObject cell: cells) {
            String cellsType = cell.getString("cells_type");
            //fill line entities
            if (Objects.equals(cellsType, "line")) {
                ModelingDataFlowLineParams lp = new ModelingDataFlowLineParams();
                lp.setUid(cell.getString("id"));
                lp.setSource(cell.getJSONObject("source").getString("cell"));
                lp.setTarget(cell.getJSONObject("target").getString("cell"));
                lineList.add(lp);
            } else {
                //fill node entities if legal
                ModelingDataFlowOperatorParams op = new ModelingDataFlowOperatorParams();
                op.setDisable(cell.getJSONObject("data").getBoolean("disabled"));
                op.setUid(cell.getString("id"));
                op.setCellType(cellsType);
                op.setData(cell.getJSONObject("data"));
                if (mainOperatorConfigNames.contains(cellsType)) {
                    //set operator to level zero if it belongs to main type
                    op.setLevel(0);
                    op.setBelongsToMainOperatorId(op.getUid());
                    //add main operator`s uid as key in result map
                    if (op.getDisable() == null || !op.getDisable()) {
                        result.put(op.getUid(),new ArrayList<>());
                    }
                }
                operatorParamsList.add(op);
            }
        }

        operatorParamsList.forEach(operatorParams -> {
            if (Objects.equals(operatorParams.getLevel(),0)) {
                operatorParams.initDataSource(operatorParamsList,lineList);
                operatorParams.sortChildren(operatorParamsList,lineList);
            }
        });

        operatorParamsList.removeIf(op -> op.getLevel() == null);

        operatorParamsList.forEach(operatorParams -> {
            String uid = operatorParams.getBelongsToMainOperatorId();
            if (result.containsKey(uid)) {
                result.get(uid).add(operatorParams);
            }
        });

        return result;
    }

    private void fillStartOperator(ModelingDataFlowSqlObject resultObj, ModelingDataFlowOperatorParams operatorParams) {
        if (operatorParams.getLevel() == 0) {
            if (operatorParams.getDataSource() != null) {
                //if it has a data source operator
                resultObj.setExecuteDbName(operatorParams.getDataSource().getData().getString("dbName"));
                resultObj.setExecuteClusterId(operatorParams.getDataSource().getData().getString("clusterId"));
                resultObj.setExecuteClusterNodeId(operatorParams.getDataSource().getData().getString("clusterNodeId"));
                resultObj.setExecuteSchema(operatorParams.getDataSource().getData().getString("schema"));
            }
            resultObj.setMainOperatorId(operatorParams.getBelongsToMainOperatorId());
            resultObj.setMainType(operatorParams.getCellType());
        }
    }


}




