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
 * OperatorSortBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorSortBuilderServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author songlei
 * @date 2022/8/15 16:35
 */
@Component("sort")
public class OperatorSortBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        String oriSql = sqlObject.getOrderByRegion();

        JSONArray sorts = params.getJSONArray("sorts");
        List<String> sqlSorts = new ArrayList<>();

        if (!sorts.isEmpty()) {
            for (int i = 0; i < sorts.size(); i++) {
                JSONObject sortItem = sorts.getJSONObject(i);
                String sortField = sortItem.getString("field");
                //if is an alias name
                if (sortField.contains("[as]")) {
                    sortField = pureAliasField(sortField);
                }
                String sortValue = sortItem.getString("value");
                if (sortField != null && sortValue !=null) {
                    sqlSorts.add(sortConvert(sortField,sortValue));
                }
            }
            if (sqlSorts.size()>0) {
                oriSql = "order by ";
                oriSql += String.join(",",sqlSorts) + " ";
            }
        }

        sqlObject.setOrderByRegion(oriSql);

        return sqlObject;
    }

    private static final HashMap<String,String> SORT_CONVERT = new HashMap<String,String>(){{
        this.put("asc","asc");
        this.put("des","desc");
    }};

    private String sortConvert(String field,String value)
    {
        switch (value) {
            case "phoneticize":
                return " NLSSORT ("+field+",'nls_sort=schinese_pinying_m')";
            default:
                return field + " " + SORT_CONVERT.get(value);
        }

    }
}
