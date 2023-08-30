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
 * ModelingDataFlowOperatorServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/impl/ModelingDataFlowOperatorServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.mapper.modeling.ModelingDataFlowOperatorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowOperatorEntity;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowOperatorService;
import java.util.ArrayList;
import java.util.List;

/**
* @author LZW
* @description modeling_data_flow_operator
* @createDate 2022-08-10 10:00:51
*/
@Service
public class ModelingDataFlowOperatorServiceImpl extends ServiceImpl<ModelingDataFlowOperatorMapper, ModelingDataFlowOperatorEntity>
    implements IModelingDataFlowOperatorService {

    @Autowired
    private ModelingDataFlowOperatorMapper modelingDataFlowOperatorMapper;

    @Override
    public List<String> selectLegalOperatorConfigNameByType(Integer type) {
        List<ModelingDataFlowOperatorEntity> result = modelingDataFlowOperatorMapper
                .selectList(
                        new QueryWrapper<ModelingDataFlowOperatorEntity>().lambda().eq(ModelingDataFlowOperatorEntity::getType, type)
                );

        List<String> configNameList = new ArrayList<>();
        for (ModelingDataFlowOperatorEntity operator : result) {
            configNameList.add(operator.getPackagePath());
        }

        return configNameList;
    }

    public List<String> selectMainOperatorConfigName() {
        List<ModelingDataFlowOperatorEntity> result = modelingDataFlowOperatorMapper
                .selectList(
                        new QueryWrapper<ModelingDataFlowOperatorEntity>().lambda().eq(ModelingDataFlowOperatorEntity::getGroupId, 1)
                );

        List<String> configNameList = new ArrayList<>();
        for (ModelingDataFlowOperatorEntity operator : result) {
            configNameList.add(operator.getPackagePath());
        }

        return configNameList;
    }
}




