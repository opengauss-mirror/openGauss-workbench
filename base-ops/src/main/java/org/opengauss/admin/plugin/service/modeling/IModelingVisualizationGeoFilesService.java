package org.opengauss.admin.plugin.service.modeling;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationGeoFilesEntity;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_geo_files
* @createDate 2022-11-07 10:42:27
*/
public interface IModelingVisualizationGeoFilesService extends IService<ModelingVisualizationGeoFilesEntity> {
    public int add(ModelingVisualizationGeoFilesEntity geoFile);
    public List<ModelingVisualizationGeoFilesEntity> selectByDataFlowId(Long dataFlowId);

}
