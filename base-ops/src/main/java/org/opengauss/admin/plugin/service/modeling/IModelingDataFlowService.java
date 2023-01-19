package org.opengauss.admin.plugin.service.modeling;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;

/**
* @author LZW
* @description modeling_data_flow
* @createDate 2022-08-10 11:41:13
*/
public interface IModelingDataFlowService extends IService<ModelingDataFlowEntity> {

    IPage<ModelingDataFlowEntity> selectList(IPage<ModelingDataFlowEntity> page, String name);

    Long insertDataFlow(ModelingDataFlowEntity dataFlowData);

    int deleteDataFlowByIds(String[] flowIds);

    int updateDataFlow(ModelingDataFlowEntity dataFlowData);

}
