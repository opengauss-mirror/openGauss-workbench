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
