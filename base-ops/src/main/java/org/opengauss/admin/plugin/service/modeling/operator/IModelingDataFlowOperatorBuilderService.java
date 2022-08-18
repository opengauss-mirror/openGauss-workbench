package org.opengauss.admin.plugin.service.modeling.operator;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public interface IModelingDataFlowOperatorBuilderService  {

    ModelingDataFlowSqlObject getOperatorSql(JSONObject params , ModelingDataFlowSqlObject sqlObject);

}
