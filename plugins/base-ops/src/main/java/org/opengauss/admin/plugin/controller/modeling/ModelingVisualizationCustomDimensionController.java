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
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;
import org.opengauss.admin.plugin.domain.model.modeling.CustomDimension;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationCustomDimensionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@RestController
@RequestMapping("/modeling/dataflow/visualization/customDimension")
public class ModelingVisualizationCustomDimensionController extends BaseController {

    @Autowired
    private IModelingVisualizationCustomDimensionsService modelingVisualizationCustomDimensionsService;

    @GetMapping("/getListByOperatorId/{operatorId}")
    public AjaxResult getListByOperatorId(@PathVariable String operatorId) {
        List<ModelingVisualizationCustomDimensionsEntity> list = modelingVisualizationCustomDimensionsService.getByOperatorId(operatorId);
        return AjaxResult.success(list);
    }

    /**
     * add
     */
    @RequestMapping("/add")
    public AjaxResult add(@RequestBody CustomDimension customDimension) {
        return toAjax(modelingVisualizationCustomDimensionsService.add(customDimension));
    }

    /**
     * delete
     */
    @DeleteMapping("/delete/{dimensionIds}")
    public AjaxResult delete(@PathVariable String[] dimensionIds) {
        return toAjax(modelingVisualizationCustomDimensionsService.deleteByIds(dimensionIds));
    }


    /**
     * save
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody CustomDimension customDimension) {
        return toAjax(modelingVisualizationCustomDimensionsService.update(customDimension));
    }

}

