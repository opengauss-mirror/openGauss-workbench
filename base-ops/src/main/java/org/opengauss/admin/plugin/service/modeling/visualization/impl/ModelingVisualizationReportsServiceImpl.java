package org.opengauss.admin.plugin.service.modeling.visualization.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationReportsEntity;
import org.opengauss.admin.plugin.mapper.modeling.ModelingVisualizationReportsMapper;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
* @author LZW
* @description modeling_visualization_reports
* @createDate 2022-09-29 13:09:24
*/
@Service
public class ModelingVisualizationReportsServiceImpl extends ServiceImpl<ModelingVisualizationReportsMapper, ModelingVisualizationReportsEntity>
    implements IModelingVisualizationReportsService {

    @Autowired
    private ModelingVisualizationReportsMapper modelingVisualizationReportsMapper;
    @Autowired
    private IModelingDataFlowService modelingDataFlowService;

    @Override
    public List<ModelingVisualizationReportsEntity> getByDataFlowIdAndName(Long dataFlowId, String name) {
        LambdaQueryWrapper<ModelingVisualizationReportsEntity> queryWrapper = Wrappers.lambdaQuery(ModelingVisualizationReportsEntity.class)
                .eq(ModelingVisualizationReportsEntity::getDataFlowId, dataFlowId)
                .orderByAsc(ModelingVisualizationReportsEntity::getId);
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(ModelingVisualizationReportsEntity::getName, name);
        }
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(ModelingVisualizationReportsEntity modelingVisualizationReports) {
        Long dataFlowId = modelingVisualizationReports.getDataFlowId();
        ModelingDataFlowEntity modelingDataFlowEntity = modelingDataFlowService.getById(dataFlowId);
        if (Objects.isNull(modelingDataFlowEntity)) {
            throw new RuntimeException("dataflow not found!");
        }

        int row = modelingVisualizationReportsMapper.insert(modelingVisualizationReports);
        return row;
    }

    @Override
    public int deleteByIds(String[] reportIds) {
        return modelingVisualizationReportsMapper.deleteBatchIds(Arrays.asList(reportIds));
    }

    @Override
    public ModelingVisualizationReportsEntity selectById(Integer reportId) {
        return modelingVisualizationReportsMapper.selectById(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ModelingVisualizationReportsEntity modelingVisualizationReports) {
        Long dataFlowId = modelingVisualizationReports.getDataFlowId();
        ModelingDataFlowEntity modelingDataFlowEntity = modelingDataFlowService.getById(dataFlowId);
        if (Objects.isNull(modelingDataFlowEntity)) {
            throw new RuntimeException("dataflow not found");
        }

        int row = modelingVisualizationReportsMapper.updateById(modelingVisualizationReports);
        return row;
    }
}




