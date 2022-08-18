package org.opengauss.admin.plugin.service.modeling.visualization.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationSnapshotsEntity;
import org.opengauss.admin.plugin.mapper.modeling.ModelingVisualizationSnapshotsMapper;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationSnapshotsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
* @author LZW
* @description modeling_visualization_snapshots
* @createDate 2022-09-23 13:44:43
*/
@Service
public class ModelingVisualizationSnapshotsServiceImpl extends ServiceImpl<ModelingVisualizationSnapshotsMapper, ModelingVisualizationSnapshotsEntity>
    implements IModelingVisualizationSnapshotsService {

    @Autowired
    private ModelingVisualizationSnapshotsMapper modelingVisualizationSnapshotsMapper;

    @Override
    public IPage<ModelingVisualizationSnapshotsEntity> selectList(IPage<ModelingVisualizationSnapshotsEntity> page, ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity) {
        LambdaQueryWrapper<ModelingVisualizationSnapshotsEntity> queryWrapper = new QueryWrapper<ModelingVisualizationSnapshotsEntity>().lambda();
        queryWrapper.eq(StringUtils.isNotEmpty(modelingVisualizationSnapshotsEntity.getId()), ModelingVisualizationSnapshotsEntity::getId, modelingVisualizationSnapshotsEntity.getId());
        queryWrapper.like(StringUtils.isNotEmpty(modelingVisualizationSnapshotsEntity.getName()), ModelingVisualizationSnapshotsEntity::getName, modelingVisualizationSnapshotsEntity.getName());

        return modelingVisualizationSnapshotsMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<ModelingVisualizationSnapshotsEntity> getByDataFlowId(Long dataFlowId) {
        LambdaQueryWrapper<ModelingVisualizationSnapshotsEntity> queryWrapper = Wrappers.lambdaQuery(ModelingVisualizationSnapshotsEntity.class)
                .eq(ModelingVisualizationSnapshotsEntity::getDataFlowId, dataFlowId);
        return list(queryWrapper);
    }

    @Override
    public int insertDataFlow(ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity) {
        int row = modelingVisualizationSnapshotsMapper.insert(modelingVisualizationSnapshotsEntity);
        return row;
    }

    @Override
    public int deleteByIds(String[] flowIds){
        int row = modelingVisualizationSnapshotsMapper.deleteBatchIds(Arrays.asList(flowIds));
        return row;
    }

    @Override
    public int updateDataFlow(ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity) {
        int row = modelingVisualizationSnapshotsMapper.updateById(modelingVisualizationSnapshotsEntity);
        return row;
    }
}




