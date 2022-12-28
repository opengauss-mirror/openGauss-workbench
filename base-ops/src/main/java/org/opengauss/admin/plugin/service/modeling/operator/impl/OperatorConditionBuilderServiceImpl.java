package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
* @author Administrator
* @createDate 2022-08-10 10:00:51
*/
@Component("condition")
public class OperatorConditionBuilderServiceImpl extends BaseBuilderServiceImpl {

    private static final String WHERE_KEYWORD = "where";
    private static final String GROUP_SEPARATOR_KEYWORD = "(";

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params , ModelingDataFlowSqlObject sqlObject) {

        //WHERE region modify
        String oriSql = sqlObject.getWhereRegion();

        JSONArray conditions = params.getJSONArray("or");

        List<String> orSqlList = new ArrayList<>();
        for (int i=0; i<conditions.size(); i++) {
            //Each group consists of N and bar components, if there are more than two groups are connected by [or]
            JSONArray andGroupParams = conditions.getJSONArray(i);
            List<String> andSqlList = new ArrayList<>();
            for (int z=0; z<andGroupParams.size(); z++) {
                JSONObject andItem = andGroupParams.getJSONObject(z);
                if (andItem.getString("field") != null) {
                    String sql = conditionConvert(andItem.getString("field"),andItem.getString("condition"),andItem.getString("value"),"string");
                    andSqlList.add(sql);
                }
            }
            if (andSqlList.size() > 0)
            {
                orSqlList.add("( "+String.join(" and ",andSqlList)+" )");
            }
        }

        if ( orSqlList.size() > 0 ) {
            if (!oriSql.contains(WHERE_KEYWORD)) {
                oriSql = "where ";
            }
            String orSqlString = String.join(" or ",orSqlList);
            //Starting from the second conditional operator, the conditions contained in each complete conditional operator are paralleled as [AND]
            if (oriSql.contains(GROUP_SEPARATOR_KEYWORD)) {
                orSqlString = " and (" + orSqlString + ")";
            }
            sqlObject.setWhereRegion(oriSql + orSqlString+" ");
        }

        return sqlObject;
    }

    public String conditionConvert(String field,String condition,String value,String valueType)
    {
        String valueTypeString = "string";
        String valueConvert = value;
        if (valueType.equals(valueTypeString)) {
            valueConvert ="'" + value + "'";
        }
        switch (condition) {
            case "equal":
                return field + " = " + valueConvert;

            case "notEqual":
                return field + " <> " + valueConvert;

            case "lessThan":
                return field + " < " + valueConvert;

            case "equalLessThan":
                return field + " <= " + valueConvert;

            case "greaterThan":
                return field + " > " + valueConvert;

            case "equalGreaterThan":
                return field + " >= " + valueConvert;

            case "include":
                return field +" like " + "'%"+value+"%'";

            case "notInclude":
                return "notlike("+field+", '%"+value+"%')";

            case "isNull":
                return field+" = ''";

            case "notNull":
                return field+" <> ''";

            default:
                return field + " " + condition + " '" + value + "'";
        }

    }

}




