package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author songlei
 * @date 2022/8/25 10:08
 */
@Component("mapping")
public class OperatorMappingBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject) {
        // mapping region modify
        JSONArray mappings = params.getJSONArray("mappings");
        List<String> sqlMapping = new ArrayList<>();

        for (int i = 0; i < mappings.size(); i++) {
            JSONObject mappingItem = mappings.getJSONObject(i);
            String field = mappingItem.getString("field");
            String value = mappingItem.getString("value");
            if (field != null && field.length()!=0 && value !=null && value.trim().length() != 0) {
                sqlMapping.add(field + " as " + value.trim());
            }
        }

        sqlObject.setMappingRegion(sqlMapping);

        return sqlObject;
    }
}
