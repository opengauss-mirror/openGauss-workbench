package org.opengauss.admin.plugin.service.modeling;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowOperatorEntity;

import java.util.List;

/**
* @author LZW
* @description modeling_data_flow_operator
* @createDate 2022-08-10 10:00:51
*/
public interface IModelingDataFlowOperatorService extends IService<ModelingDataFlowOperatorEntity> {
    List<String> selectLegalOperatorConfigNameByType(Integer type);
    List<String> selectMainOperatorConfigName();

}
