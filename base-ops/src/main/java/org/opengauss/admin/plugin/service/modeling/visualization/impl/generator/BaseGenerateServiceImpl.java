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
 * BaseGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/BaseGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.IModelingDataBaseService;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowProcessService;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationChartGenerateService;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationCustomDimensionsService;
import org.opengauss.admin.plugin.vo.modeling.Categories;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class BaseGenerateServiceImpl implements IModelingVisualizationChartGenerateService {

    @Autowired
    private IModelingDataFlowProcessService modelingDataFlowProcessService;
    @Autowired
    private IModelingDataFlowService modelingDataFlowService;
    @Autowired
    private IModelingDataBaseService modelingDataBaseService;
    @Autowired
    public IModelingVisualizationCustomDimensionsService modelingVisualizationCustomDimensionsService;


    public JSONObject fullParams;
    public JSONObject visualizationParams;

    public List<Map<String, Object>> queryResult;

    @Override
    public void setFullParams(JSONObject fullParams) { this.fullParams = fullParams;}

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        return null;
    }

    public void parseParams() throws SQLException, ClassNotFoundException {
        JSONObject dataFlowContent = fullParams.getJSONObject("operatorsData");
        String dataFlowId = fullParams.getString("dataFlowId");
        visualizationParams = fullParams.getJSONObject("paramsData");

        ModelingDataFlowEntity modelingDataFlowEntity = modelingDataFlowService.getById(fullParams.getString("dataFlowId"));

        if (dataFlowContent == null || modelingDataFlowEntity == null)
        {
            throw new RuntimeException("operators info is empty!");
        }

        ModelingDataFlowSqlObject sqlObject = modelingDataFlowProcessService.getSqlObjectByTargetOperatorId(dataFlowContent, visualizationParams.getString("operatorId"),dataFlowId);

        if (sqlObject == null)
        {
            throw new RuntimeException("build sql object failed!");
        }

        sqlObject.initDataBase(sqlObject.getExecuteClusterNode(), modelingDataFlowEntity.getSchema());
        queryResult = modelingDataBaseService.queryWithSqlObject(sqlObject);
    }


    public Dimension formatDimension(Dimension dimension) {
        //if dimension is from custom
        if (dimension.getField().contains("custom|"))
        {
            //get custom dimension record in database
            String customId = dimension.getField().replace("custom|","");
            ModelingVisualizationCustomDimensionsEntity cd = modelingVisualizationCustomDimensionsService.getById(customId);
            dimension.setField(cd.getField());
            dimension.setCustomFlag(true);
            //enum value by field
            JSONArray categoriesJson = JSONArray.parseArray(cd.getCategoriesJson());
            dimension.setNum(categoriesJson.size());
            categoriesJson.forEach(categoryJson->{
                Categories category = JSON.parseObject(JSON.toJSONString(categoryJson), Categories.class);
                dimension.addCategoryBody(category.toBody(cd.getField()));
            });
        } else {
            //enum value by field
            queryResult.forEach(item->{
                String filedValue = (String) item.get(dimension.getField());
                if (filedValue != null) {
                    Categories category = new Categories();
                    category.setName(filedValue);
                    category.setValue(List.of(filedValue));
                    category.setType(1);
                    dimension.addCategoryBody(category.toBody(dimension.getField()));
                }
            });
        }

        return dimension;
    }

    public String formatDateTimeDimension(Timestamp value , Integer particle) {
        if (value == null) {
            return null;
        }
        //default format yyyy-mm-dd , ignore min,sec
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        switch (particle) {
            case 1: {
                sdf = new SimpleDateFormat("yyyy年");
                break;
            }
            case 2: {
                sdf = new SimpleDateFormat("yyyy年MM月");
                break;
            }
            case 3: {
                sdf = new SimpleDateFormat("yyyy年MM月dd日");
                break;
            }
            case 4: {
                sdf = new SimpleDateFormat("HH时");
                break;
            }
            default:{
                break;
            }
        }
        return sdf.format(new Date(value.getTime()));
    }
}




