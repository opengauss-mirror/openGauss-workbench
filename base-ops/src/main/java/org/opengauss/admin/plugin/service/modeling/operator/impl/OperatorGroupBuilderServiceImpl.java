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
 * OperatorGroupBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorGroupBuilderServiceImpl.java
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
