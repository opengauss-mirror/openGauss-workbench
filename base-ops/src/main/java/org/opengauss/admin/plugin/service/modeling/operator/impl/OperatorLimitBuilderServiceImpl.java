package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

/**
 * @author songlei
 * @date 2022/8/16 9:46
 */
@Component("limit")
public class OperatorLimitBuilderServiceImpl extends BaseBuilderServiceImpl {
    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        //limit region modify
        String oriSql = sqlObject.getLimitRegion();

        JSONObject restriction = params.getJSONObject("restriction");

        if (!restriction.isEmpty()) {
            String skip = restriction.getString("skip");

            String limitCount = restriction.getString("limitCount");
            if (skip!=null) {
                skip = skip+",";
            }else {
                skip = "";
            }

            if (limitCount!=null) {
                oriSql += "limit "+skip+limitCount+" ";
            }
        }

        sqlObject.setLimitRegion(oriSql);

        return sqlObject;
    }

}
