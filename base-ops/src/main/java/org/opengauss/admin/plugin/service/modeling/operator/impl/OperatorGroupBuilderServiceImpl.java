package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author songlei
 * @date 2022/8/15 16:35
 */
@Component("group")
public class OperatorGroupBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        //group region modify
        String oriSql = sqlObject.getGroupRegion();

        JSONArray groupFields = params.getJSONArray("groups");
        List<String> sqlGroupFields = new ArrayList<>();

        if (!groupFields.isEmpty()) {
            for (int i = 0; i < groupFields.size(); i++) {
                sqlGroupFields.add(groupFields.getJSONObject(i).getString("field"));
            }
            if (sqlGroupFields.size()>0) {
                oriSql = "group by ";
                oriSql += String.join(",",sqlGroupFields) + " ";
            }
        }

        sqlObject.setGroupRegion(oriSql);

        return sqlObject;
    }
}
