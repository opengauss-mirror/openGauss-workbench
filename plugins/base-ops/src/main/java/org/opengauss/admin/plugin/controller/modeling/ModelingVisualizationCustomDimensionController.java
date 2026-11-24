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

