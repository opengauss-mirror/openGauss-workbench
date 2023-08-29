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
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationSnapshotsEntity;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationSnapshotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@RestController
@RequestMapping("/modeling/dataflow/visualization/snapshot")
public class ModelingVisualizationSnapshotController extends BaseController {

    @Autowired
    private IModelingVisualizationSnapshotsService modelingVisualizationSnapshotsService;

    @GetMapping("/getListByDataFlowId/{flowId}")
    public AjaxResult getListByDataFlowId(@PathVariable Long flowId) {
        List<ModelingVisualizationSnapshotsEntity> list = modelingVisualizationSnapshotsService.getByDataFlowId(flowId);
        return AjaxResult.success(list);
    }
    /**
     * add
     */
    @RequestMapping("/add")
    public AjaxResult add(@RequestBody ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity) {
        return toAjax(modelingVisualizationSnapshotsService.insertDataFlow(modelingVisualizationSnapshotsEntity));
    }

    /**
     * delete
     */
    @DeleteMapping("/delete/{snapshotIds}")
    public AjaxResult delete(@PathVariable String[] snapshotIds) {
        return toAjax(modelingVisualizationSnapshotsService.deleteByIds(snapshotIds));
    }


    /**
     * save
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity) {
        return toAjax(modelingVisualizationSnapshotsService.updateDataFlow(modelingVisualizationSnapshotsEntity));
    }

}

