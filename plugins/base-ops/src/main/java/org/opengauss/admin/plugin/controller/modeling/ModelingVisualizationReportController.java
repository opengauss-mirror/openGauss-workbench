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

import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationReportsEntity;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@RestController
@RequestMapping("/modeling/dataflow/visualization/report")
public class ModelingVisualizationReportController extends BaseController {

    @Autowired
    private IModelingVisualizationReportsService modelingVisualizationReportsService;

    @GetMapping("/getListByDataFlowId/{flowId}")
    public AjaxResult getListByDataFlowId(@PathVariable Long flowId,@RequestParam(value = "name",required = false) String name) {
        List<ModelingVisualizationReportsEntity> list = modelingVisualizationReportsService.getByDataFlowIdAndName(flowId,name);
        return AjaxResult.success(list);
    }

    @GetMapping("/share/getByReportId/{reportId}")
    public AjaxResult getByReportId(@PathVariable Long reportId) {
        return AjaxResult.success(modelingVisualizationReportsService.getById(reportId));
    }

    /**
     * add
     */
    @RequestMapping("/add")
    public AjaxResult add(@RequestBody ModelingVisualizationReportsEntity modelingVisualizationReports) {
        return toAjax(modelingVisualizationReportsService.add(modelingVisualizationReports));
    }

    /**
     * delete
     */
    @DeleteMapping("/delete/{reportIds}")
    public AjaxResult delete(@PathVariable String[] reportIds) {
        return toAjax(modelingVisualizationReportsService.deleteByIds(reportIds));
    }


    /**
     * save
     */
    @PutMapping("/update")
    public AjaxResult edit(@RequestBody ModelingVisualizationReportsEntity modelingVisualizationReports) {
        return toAjax(modelingVisualizationReportsService.update(modelingVisualizationReports));
    }

}

