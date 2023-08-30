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
 * OperatorMappingBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorMappingBuilderServiceImpl.java
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
