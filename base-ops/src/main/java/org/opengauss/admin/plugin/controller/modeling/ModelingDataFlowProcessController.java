/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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

