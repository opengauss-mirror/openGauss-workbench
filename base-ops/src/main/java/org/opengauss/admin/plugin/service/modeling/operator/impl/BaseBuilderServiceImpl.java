package org.opengauss.admin.plugin.service.modeling.operator.impl;


import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowOperatorBuilderService;

/**
 * @author songlei
 * @date 2022/8/25 10:08
 */
public class BaseBuilderServiceImpl implements IModelingDataFlowOperatorBuilderService {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject) {
        return sqlObject;
    }

    public String formatField(String field) {
        if (field.contains(".")) {
            return "\"" + field.split("\\.")[0] + "\"." + field.split("\\.")[1];
        }
        return field;
    }

    public String pureField(String field) {
        if (field.contains(".")) {
            return field.split("\\.")[1];
        }
        return field;
    }
}
