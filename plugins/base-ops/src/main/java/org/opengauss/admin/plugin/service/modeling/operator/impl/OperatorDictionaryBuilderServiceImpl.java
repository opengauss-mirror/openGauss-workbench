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
 * OperatorDictionaryBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorDictionaryBuilderServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

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
