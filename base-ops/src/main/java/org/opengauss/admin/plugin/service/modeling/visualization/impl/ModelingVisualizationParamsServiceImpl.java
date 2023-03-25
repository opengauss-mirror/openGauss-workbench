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
 * ModelingVisualizationParamsServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/ModelingVisualizationParamsServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationParamsEntity;
import org.opengauss.admin.plugin.mapper.modeling.ModelingVisualizationParamsMapper;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationChartGenerateService;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationParamsService;
import org.opengauss.admin.plugin.service.modeling.visualization.factory.ModelingVisualizationChartFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
* @author LZW
* @description modeling_visualization_params
* @createDate 2022-09-23 13:44:13
*/
@Service
public class ModelingVisualizationParamsServiceImpl extends ServiceImpl<ModelingVisualizationParamsMapper, ModelingVisualizationParamsEntity>
    implements IModelingVisualizationParamsService {

    @Autowired
    private ModelingVisualizationParamsMapper modelingVisualizationParamsMapper;
    @Autowired
    private IModelingDataFlowService modelingDataFlowService;
    @Autowired
    ModelingVisualizationChartFactory modelingVisualizationChartFactory;

    @Override
    public IPage<ModelingVisualizationParamsEntity> selectList(IPage<ModelingVisualizationParamsEntity> page, ModelingVisualizationParamsEntity modelingVisualizationParamsEntity) {
        return null;
    }

    @Override
    public List<ModelingVisualizationParamsEntity> getByOperatorId(String operatorId) {
        LambdaQueryWrapper<ModelingVisualizationParamsEntity> queryWrapper = Wrappers.lambdaQuery(ModelingVisualizationParamsEntity.class)
                .eq(ModelingVisualizationParamsEntity::getOperatorId, operatorId)
                .orderByAsc(ModelingVisualizationParamsEntity::getId);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(ModelingVisualizationParamsEntity modelingVisualizationParamsEntity) {
        Long dataFlowId = modelingVisualizationParamsEntity.getDataFlowId();
        ModelingDataFlowEntity modelingDataFlowEntity = modelingDataFlowService.getById(dataFlowId);
        if (Objects.isNull(modelingDataFlowEntity)) {
            throw new RuntimeException("dataflow not found");
        }

        int row = modelingVisualizationParamsMapper.insert(modelingVisualizationParamsEntity);
        return row;
    }

    @Override
    public int deleteByIds(String[] paramIds){
        int row = modelingVisualizationParamsMapper.deleteBatchIds(Arrays.asList(paramIds));
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ModelingVisualizationParamsEntity modelingVisualizationParamsEntity) {
        Long dataFlowId = modelingVisualizationParamsEntity.getDataFlowId();
        ModelingDataFlowEntity modelingDataFlowEntity = modelingDataFlowService.getById(dataFlowId);
        if (Objects.isNull(modelingDataFlowEntity)) {
            throw new RuntimeException("dataflow not found");
        }
        int row = modelingVisualizationParamsMapper.updateById(modelingVisualizationParamsEntity);
        return row;
    }

    @Override
    public String generateChart(JSONObject params) throws Exception {
        IModelingVisualizationChartGenerateService generator = modelingVisualizationChartFactory.getGenerateServiceByJsonParams(params);
        return generator.generate();
    }
}




