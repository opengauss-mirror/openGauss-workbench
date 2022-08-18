package org.opengauss.admin.plugin.service.modeling.operator;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public interface IModelingDataFlowProcessService {

    List<ModelingDataFlowSqlObject> getAllSqlObject(JSONObject params);

    ModelingDataFlowSqlObject getSqlObjectByTargetOperatorId(JSONObject params, String operatorId, String dataFlowId);

    String getMainQueryType(JSONObject params);

    Integer getQueryCount(JSONObject params);

}
