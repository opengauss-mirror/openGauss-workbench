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

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.IModelingDataBaseService;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@RestController
@RequestMapping("/modeling/dataflow")
public class ModelingDataFlowProcessController extends BaseController {

    @Autowired
    private IModelingDataFlowProcessService modelingDataFlowProcessService;
    @Autowired
    private IModelingDataBaseService modelingDataBaseService;
    @Autowired
    private IModelingDataFlowService modelingDataFlowService;

    @PostMapping(value = "/getSql")
    public AjaxResult getSql(@RequestBody JSONObject param) {
        AjaxResult ajax = AjaxResult.success();
        Map<String,String> result = new HashMap<>();
        modelingDataFlowProcessService.getAllSqlObject(param).forEach(sqlObj -> {
            result.put(sqlObj.getMainOperatorId(),sqlObj.toSql());
        });
        ajax.put("data",result);
        return ajax;
    }

    @PostMapping(value = "/runSql")
    public AjaxResult runSql(@RequestBody JSONObject param) {
        AjaxResult ajax = AjaxResult.success();
        Map<String,Object> result = new HashMap<>();
        modelingDataFlowProcessService.getAllSqlObject(param).forEach(sqlObj -> {
            result.put(sqlObj.getMainOperatorId(),modelingDataBaseService.getSqlResult(sqlObj));
        });
        ajax.put("data",result);
        return ajax;
    }

    /**
     * get result fields when sql running to target operator
     */
    @PostMapping(value = "/getResultFieldsByOperator")
    public AjaxResult getResultFieldsByOperator(@RequestBody JSONObject params) {
        AjaxResult ajax = AjaxResult.success();
        ModelingDataFlowSqlObject sqlObject = modelingDataFlowProcessService.getSqlObjectByTargetOperatorId(params,params.getString("id"),params.getString("dataFlowId"));
        List<Map<String, Object>> result;
        try {
            result = modelingDataBaseService.queryWithSqlObject(sqlObject);
        } catch (SQLException | ClassNotFoundException e) {
            return AjaxResult.error("Database query error.Please check exception info : "+ e.getMessage());
        }
        if (result.size() == 0) {
            return AjaxResult.error("Query no data when running to this operator!");
        }
        ajax.put("dimension_options", result.get(0).keySet());
        ajax.put("query_data", result);
        return ajax;
    }

}

