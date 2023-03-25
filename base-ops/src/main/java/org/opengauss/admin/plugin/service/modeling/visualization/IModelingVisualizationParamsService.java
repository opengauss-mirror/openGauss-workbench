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
 * IModelingVisualizationParamsService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/IModelingVisualizationParamsService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationParamsEntity;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_params
* @createDate 2022-09-23 13:44:13
*/
public interface IModelingVisualizationParamsService extends IService<ModelingVisualizationParamsEntity> {

    IPage<ModelingVisualizationParamsEntity> selectList(IPage<ModelingVisualizationParamsEntity> page, ModelingVisualizationParamsEntity modelingVisualizationParamsEntity);
    public List<ModelingVisualizationParamsEntity> getByOperatorId(String operatorId);
    public int add(ModelingVisualizationParamsEntity modelingVisualizationParamsEntity);
    public int deleteByIds(String[] paramIds);
    public int update(ModelingVisualizationParamsEntity modelingVisualizationParamsEntity);
    public String generateChart(JSONObject params) throws Exception;
}
