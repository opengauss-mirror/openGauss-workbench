package org.opengauss.admin.plugin.service.modeling.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationGeoFilesEntity;
import org.opengauss.admin.plugin.mapper.modeling.ModelingVisualizationGeoFilesMapper;
import org.opengauss.admin.plugin.service.modeling.IModelingVisualizationGeoFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_geo_files
* @createDate 2022-11-07 10:42:27
*/
@Service
public class ModelingVisualizationGeoFilesServiceImpl extends ServiceImpl<ModelingVisualizationGeoFilesMapper, ModelingVisualizationGeoFilesEntity>
    implements IModelingVisualizationGeoFilesService {

    @Autowired
    private ModelingVisualizationGeoFilesMapper modelingVisualizationGeoFilesMapper;

    @Override
    public int add(ModelingVisualizationGeoFilesEntity geoFile) {
        return modelingVisualizationGeoFilesMapper.insert(geoFile);
    }

    @Override
    public List<ModelingVisualizationGeoFilesEntity> selectByDataFlowId(Long dataFlowId) {
        List<ModelingVisualizationGeoFilesEntity> result = modelingVisualizationGeoFilesMapper
                .selectList(
                        new QueryWrapper<ModelingVisualizationGeoFilesEntity>().lambda().eq(ModelingVisualizationGeoFilesEntity::getDataFlowId, dataFlowId)
                );

        return result;
    }
}




