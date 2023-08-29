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
 * OperatorJoinBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorJoinBuilderServiceImpl.java
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

/**
* @author Administrator
* @createDate 2022-08-10 10:00:51
*/
@Component("join")
public class OperatorJoinBuilderServiceImpl extends BaseBuilderServiceImpl {


    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params , ModelingDataFlowSqlObject sqlObject) {

        // join region modify
        String oriSql = sqlObject.getJoinRegion();

        String joinTable = params.getString("table");
        String joinType = params.getString("connectType");
        JSONArray joinConditions = params.getJSONArray("condition");

        String joinSql = joinConvert(joinType,joinTable,joinConditions);
        if (joinSql.length()>0) {
            oriSql += joinSql;
        }

        sqlObject.setJoinRegion(oriSql);

        return sqlObject;
    }

    public String joinConvert(String joinType, String joinTable,JSONArray joinConditions) {
        String joinSql = "";
        joinType = joinTypeConvert(joinType);
        String joinCondition = joinConditionsConvert(joinConditions);

        if (joinType.length()>0 && joinTable.length()>0 && joinCondition.length()>0) {
            joinSql = " "+joinType+" "+joinTable+ " "+joinCondition+" ";
        }
        return joinSql;
    }

    public String joinTypeConvert(String joinType)
    {
        switch (joinType) {
            case "innerJoin":
                return "join ";

            case "leftJoin":
                return "left join ";

            case "rightJoin":
                return "right join ";

            case "fullJoin":
                return "full join ";

            default:
                return "";
        }

    }

    public String joinConditionsConvert(JSONArray conditions) {
        String conditionSql = "";

        List<String> orSqlList = new ArrayList<>();
        OperatorConditionBuilderServiceImpl operatorConditionBuilderServiceImpl = new OperatorConditionBuilderServiceImpl();

        for (int i=0; i<conditions.size(); i++) {
            JSONArray andGroupParams = conditions.getJSONArray(i);
            List<String> andSqlList = new ArrayList<>();
            for (int z=0; z<andGroupParams.size(); z++) {
                JSONObject andItem = andGroupParams.getJSONObject(z);
                String sql = operatorConditionBuilderServiceImpl.conditionConvert(andItem.getString("field"),andItem.getString("condition"),andItem.getString("value"));
                andSqlList.add(sql);
            }
            if (andSqlList.size() > 0)
            {
                orSqlList.add("( "+String.join(" and ",andSqlList)+" )");
            }
        }

        if ( orSqlList.size() > 0 ) {

            conditionSql = " on "+" "+String.join(" or ",orSqlList);

        }

        return conditionSql;

    }

}




