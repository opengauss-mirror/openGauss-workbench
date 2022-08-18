package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@Component("delete")
public class OperatorDeleteBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params , ModelingDataFlowSqlObject sqlObject) {
        // delete region init
        String oriSql = sqlObject.getDeleteRegion();
        String tableName = params.getString("table");
        oriSql += tableName+" ";
        sqlObject.setDeleteRegion(oriSql);

        return sqlObject;
    }
}
