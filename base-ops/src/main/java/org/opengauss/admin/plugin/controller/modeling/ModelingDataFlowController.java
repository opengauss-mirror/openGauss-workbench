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

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.OperatorType;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@RestController
@RequestMapping("/modeling/dataflow")
public class ModelingDataFlowController extends BaseController {

    @Autowired
    private IModelingDataFlowService modelingDataFlowService;
    @Autowired
    private IModelingDataFlowProcessService modelingDataFlowProcessService;

    @GetMapping("/list")
    public AjaxResult page(@RequestParam(required = false, value = "name") String name) {
        IPage<ModelingDataFlowEntity> page = modelingDataFlowService.selectList(startPage(), name);
        AjaxResult result = AjaxResult.success();
        result.put("data", Map.of("total",page.getTotal()));
        result.put("rows",page.getRecords());
        return result;
    }

    @GetMapping("/getById/{dataFlowId}")
    public AjaxResult get(@PathVariable String dataFlowId){
        return AjaxResult.success(modelingDataFlowService.getById(dataFlowId));
    }

    /**
     * add
     */
    @Log(title = "base-ops-dataflow",operatorType = OperatorType.PLUGIN,businessType = BusinessType.INSERT)
    @RequestMapping("/add")
    public AjaxResult add(@RequestBody ModelingDataFlowEntity dataFlowData) {
        if (modelingDataFlowService.findByName(dataFlowData.getName()).isEmpty()) {
            return AjaxResult.success(modelingDataFlowService.insertDataFlow(dataFlowData));
        } else {
            return AjaxResult.error("The data flow with the same name already exists, please try again with a different name.");
        }
    }

    /**
     * delete
     */
    @Log(title = "dataflow",operatorType = OperatorType.PLUGIN,businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{flowIds}")
    public AjaxResult remove(@PathVariable String[] flowIds) {
        return toAjax(modelingDataFlowService.deleteDataFlowByIds(flowIds));
    }

    /**
     * save
     */
    @Log(title = "dataflow",operatorType = OperatorType.PLUGIN,businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody ModelingDataFlowEntity dataFlowData) {
        if (dataFlowData.getOperatorContent() != null) {
            if (dataFlowData.getOperatorContent().equals(modelingDataFlowService.getById(dataFlowData.getId()).getOperatorContent())) {
                return AjaxResult.success("nothing changed");
            } else {
                dataFlowData.setType("query");
            }
        }

        return toAjax(modelingDataFlowService.updateDataFlow(dataFlowData));
    }

    /**
     * distribution info for home page
     */
    @GetMapping("/distribution")
    public AjaxResult distribution() {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("distribution", modelingDataFlowService.queryGroupByType());
        return ajax;
    }

    /**
     * processInfo for home page
     */
    @GetMapping("/processInfo")
    public AjaxResult processInfo() {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("distribution", modelingDataFlowService.queryProcessInfo());
        return ajax;
    }

    /**
     * count for home page
     */
    @GetMapping("/count")
    public AjaxResult count() {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("count", modelingDataFlowService.count());
        return ajax;
    }
}

