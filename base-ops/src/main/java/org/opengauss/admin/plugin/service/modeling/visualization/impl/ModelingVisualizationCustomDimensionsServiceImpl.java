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
 * ModelingVisualizationCustomDimensionsServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/ModelingVisualizationCustomDimensionsServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;
import org.opengauss.admin.plugin.domain.model.modeling.CustomDimension;
import org.opengauss.admin.plugin.mapper.modeling.ModelingVisualizationCustomDimensionsMapper;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationCustomDimensionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
* @author LZW
* @description modeling_visualization_custom_dimensions
* @createDate 2022-10-18 10:54:56
*/
@Service
public class ModelingVisualizationCustomDimensionsServiceImpl extends ServiceImpl<ModelingVisualizationCustomDimensionsMapper, ModelingVisualizationCustomDimensionsEntity>
    implements IModelingVisualizationCustomDimensionsService {

    @Autowired
    private ModelingVisualizationCustomDimensionsMapper modelingVisualizationCustomDimensionsMapper;

    @Override
    public List<ModelingVisualizationCustomDimensionsEntity> getByOperatorId(String operatorId) {
        LambdaQueryWrapper<ModelingVisualizationCustomDimensionsEntity> queryWrapper = Wrappers.lambdaQuery(ModelingVisualizationCustomDimensionsEntity.class)
                .eq(ModelingVisualizationCustomDimensionsEntity::getOperatorId, operatorId)
                .orderByAsc(ModelingVisualizationCustomDimensionsEntity::getId);
        return list(queryWrapper);
    }

    @Override
    public int add(CustomDimension customDimension) {
        ModelingVisualizationCustomDimensionsEntity modelingVisualizationCustomDimensionsEntity = new ModelingVisualizationCustomDimensionsEntity();
        int row = modelingVisualizationCustomDimensionsMapper.insert(modelingVisualizationCustomDimensionsEntity.from(customDimension));
        return row;
    }

    @Override
    public int deleteByIds(String[] paramIds) {
        int row = modelingVisualizationCustomDimensionsMapper.deleteBatchIds(Arrays.asList(paramIds));
        return row;
    }

    @Override
    public int update(CustomDimension customDimension) {
        ModelingVisualizationCustomDimensionsEntity modelingVisualizationCustomDimensionsEntity = new ModelingVisualizationCustomDimensionsEntity();
        int row = modelingVisualizationCustomDimensionsMapper.updateById(modelingVisualizationCustomDimensionsEntity.from(customDimension));
        return row;
    }
}




