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
