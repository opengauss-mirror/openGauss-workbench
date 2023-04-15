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
 * OperatorPolymerizationBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorPolymerizationBuilderServiceImpl.java
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
 * @author songlei
 * @date 2022/8/15 16:35
 */
@Component("polymerization")
public class OperatorPolymerizationBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        // Polymerization region modify
        String oriSql = sqlObject.getPolymerizationRegion();

        JSONArray polymerizations = params.getJSONArray("polymerization");
        List<String> sqlPolymerization = new ArrayList<>();

        if (!polymerizations.isEmpty()) {
            for (int i = 0; i < polymerizations.size(); i++) {
                JSONObject polymerizationItem = polymerizations.getJSONObject(i);
                String way = polymerizationItem.getString("way");
                String field = polymerizationItem.getString("field");
                String alias = polymerizationItem.getString("alias");
                if (way != null && field !=null) {
                    String polymerField = polymerizationConvert(way, field);
                    sqlPolymerization.add(polymerField + " as " + alias);
                    //add field to group by region
                    if ("year".equals(way) || "month".equals(way) || "day".equals(way)) {
                        String oldGroupBy = sqlObject.getGroupRegion();
                        if (oldGroupBy == null || oldGroupBy.isEmpty()) {
                            sqlObject.setGroupRegion("group by " + polymerField + " ");
                        } else {
                            sqlObject.setGroupRegion(oldGroupBy + "," + polymerField + " ");
                        }
                    }
                }
            }

            if (sqlPolymerization.size()>0) {
                oriSql += String.join(",",sqlPolymerization) + " ";
            }
        }

        sqlObject.setPolymerizationRegion(oriSql);

        return sqlObject;
    }

    private String polymerizationConvert(String way,String field) {
        String sql;
        switch (way) {
            case "max":
                sql = "COALESCE( max("+field+") , 0)";
                break;
            case "min":
                sql = "COALESCE( min("+field+") , 0)";
                break;
            case "avg":
                sql = "COALESCE( avg("+field+") , 0)";
                break;
            case "sum":
                sql = "COALESCE( sum("+field+") , 0)";
                break;
            case "count":
                sql = "count("+field+")";
                break;
            case "year":
                sql = "TO_CHAR( "+field+",'YYYY')";
                break;
            case "month":
                sql = "TO_CHAR( "+field+",'YYYY-MM')";
                break;
            default:
                sql = field;
                break;
        }

        return sql;
    }
}
