package org.opengauss.admin.plugin.service.modeling.visualization;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationSnapshotsEntity;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_snapshots
* @createDate 2022-09-23 13:44:43
*/
public interface IModelingVisualizationSnapshotsService extends IService<ModelingVisualizationSnapshotsEntity> {
    IPage<ModelingVisualizationSnapshotsEntity> selectList(IPage<ModelingVisualizationSnapshotsEntity> page, ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity);

    public List<ModelingVisualizationSnapshotsEntity> getByDataFlowId(Long dataFlowId);
    int insertDataFlow(ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity);
    int deleteByIds(String[] flowIds);
    int updateDataFlow(ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity);
}
