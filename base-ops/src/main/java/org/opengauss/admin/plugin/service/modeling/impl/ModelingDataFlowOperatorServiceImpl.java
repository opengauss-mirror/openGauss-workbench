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




