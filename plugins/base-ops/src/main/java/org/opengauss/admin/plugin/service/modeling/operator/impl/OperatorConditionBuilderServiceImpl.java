/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OperatorConditionBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorConditionBuilderServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                    String preparedParam;
                    String condition = andItem.getString("condition");

                    if (Objects.equals(condition, "include") || Objects.equals(condition, "notInclude")) {
                        preparedParam = sqlObject.addParam("%" + andItem.getString("value") + "%");
                    } else if (Objects.equals(condition, "in") || Objects.equals(condition, "notIn")) {
                        List<String> values = List.of(andItem.getString("value").split(","));
                        preparedParam = sqlObject.addParamList(values);
                    } else {
                        preparedParam = sqlObject.addParam(andItem.getString("value"));
                    }
                    String sql = conditionConvert(andItem.getString("field"),condition,preparedParam);
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

    public String conditionConvert(String field,String condition,String value)
    {

        switch (condition) {
            case "equal":
                return field + " = " + value;

            case "notEqual":
                return field + " <> " + value;

            case "lessThan":
                return field + " < " + value;

            case "equalLessThan":
                return field + " <= " + value;

            case "greaterThan":
                return field + " > " + value;

            case "equalGreaterThan":
                return field + " >= " + value;

            case "include":
                return field +" like " + value;

            case "notInclude":
                return "notlike("+field+"," +value+ ")";

            case "isNull":
                return field+" is NULL";

            case "notNull":
                return field+" is NOT NULL";

            case "lengthLessThan":
                return "length("+field+") < " + value;

            case "lengthLongerThan":
                return "length("+field+") > " + value;

            case "in":
                return field + " IN (" + value + ")";

            case "notIn":
                return field + " NOT IN " + value + ")";

            default:
                return field + " " + condition + value ;
        }
    }
}




