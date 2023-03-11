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
            String limitCount = restriction.getString("limitCount");
            if (limitCount != null) {
                oriSql += "limit "+ limitCount + " ";
            }

            String skip = restriction.getString("skip");
            if (skip != null) {
                oriSql += "offset " + skip + " ";
            }
        }

        sqlObject.setLimitRegion(oriSql);

        return sqlObject;
    }

}
