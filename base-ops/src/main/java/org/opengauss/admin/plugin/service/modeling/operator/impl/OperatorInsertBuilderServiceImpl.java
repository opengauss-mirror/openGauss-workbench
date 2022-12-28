package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@Component("insert")
public class OperatorInsertBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params , ModelingDataFlowSqlObject sqlObject) {
        // insert region init
        String oriSql = sqlObject.getInsertRegion();
        String tableName = params.getString("table");
        oriSql += tableName+" ";
        sqlObject.setInsertRegion(oriSql);

        //modify insert fields
        String insertFields = sqlObject.getInsertFieldsRegion();
        //get fields to insert
        List<String> insertFieldString = new ArrayList<>();
        List<List<String>> insertValueString = new ArrayList<>();

        JSONArray prepareInsertData = params.getJSONArray("list1");
        if (!prepareInsertData.isEmpty()) {
            for (int i = 0; i < prepareInsertData.size(); i++) {
                JSONArray fieldDataList = prepareInsertData.getJSONArray(i);
                List<String> childValue = new ArrayList<>();
                for (int z = 0; z < fieldDataList.size(); z++) {
                    JSONObject data = fieldDataList.getJSONObject(z);
                    String field = data.getString("field").split("\\.")[1];
                    String value = data.getString("value");
                    childValue.add(z,value);
                    if (!insertFieldString.contains(field)) {
                        insertFieldString.add(z,field);
                    }
                }
                insertValueString.add(i,childValue);
            }
        }

        insertFields = "("+String.join(",",insertFieldString)+") " + insertFields;
        List<String> insertValue = new ArrayList<>();
        for (List<String> value:insertValueString) {
            insertValue.add("("+String.join(",",value)+")");
        }
        insertFields = insertFields + String.join(",",insertValue) + " ";
        sqlObject.setInsertFieldsRegion(insertFields);

        return sqlObject;
    }
}
