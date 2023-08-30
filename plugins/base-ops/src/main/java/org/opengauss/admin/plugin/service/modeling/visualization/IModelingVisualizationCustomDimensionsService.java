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
 * IModelingVisualizationCustomDimensionsService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/IModelingVisualizationCustomDimensionsService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;
import org.opengauss.admin.plugin.domain.model.modeling.CustomDimension;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_custom_dimensions
* @createDate 2022-10-18 10:54:56
*/
public interface IModelingVisualizationCustomDimensionsService extends IService<ModelingVisualizationCustomDimensionsEntity> {
    public List<ModelingVisualizationCustomDimensionsEntity> getByOperatorId(String operatorId);
    public int add(CustomDimension customDimension);
    public int deleteByIds(String[] paramIds);
    public int update(CustomDimension customDimension);
}
