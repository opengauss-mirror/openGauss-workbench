package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

/**
 * @author songlei
 * @date 2022/8/25 10:08
 */
@Component("dictionary")
public class OperatorDictionaryBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject) {

        String dictionaryTable = params.getString("table");
        String matchField = params.getString("matchField");
        String sourceFiled = params.getString("field");
        String rigidField = params.getString("rigidField");

        if (!dictionaryTable.isEmpty()
                && !matchField.isEmpty()
                && !sourceFiled.isEmpty()
                && !rigidField.isEmpty()) {
            // join region modify
            String oriSql = sqlObject.getJoinRegion();
            String joinSql = oriSql + " left join \"" + dictionaryTable + "\" on " + formatField(matchField) + " = " + pureField(sourceFiled) + " ";
            sqlObject.setJoinRegion(joinSql);

            //mapping region modify
            String mappingSql = formatField(rigidField) + " as " + sourceFiled.split("\\.")[1];
            sqlObject.getMappingRegion().add(mappingSql);
        }

        return sqlObject;
    }
}
