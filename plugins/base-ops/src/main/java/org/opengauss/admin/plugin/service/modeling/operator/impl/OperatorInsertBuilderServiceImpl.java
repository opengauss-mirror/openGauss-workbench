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
 * OperatorInsertBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorInsertBuilderServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.*;

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
        List<String>  insertFieldString = new ArrayList<>();
        List<List<String>> insertValueString = new ArrayList<>();

        JSONArray prepareInsertData = params.getJSONArray("list1");
        if (!prepareInsertData.isEmpty()) {

            for (int i = 0; i < prepareInsertData.size(); i++) {
                JSONArray fieldDataList = prepareInsertData.getJSONArray(i);
                for (int z = 0; z < fieldDataList.size(); z++) {
                    JSONObject data = fieldDataList.getJSONObject(z);
                    String field = data.getString("field").split("\\.")[1];
                    if (!insertFieldString.contains(field)) {
                        insertFieldString.add(field);
                    }
                }
            }

            for (int i = 0; i < prepareInsertData.size(); i++) {
                JSONArray fieldDataList = prepareInsertData.getJSONArray(i);
                List<String> childValue = new ArrayList<>(Collections.nCopies(insertFieldString.size(), "''"));
                for (int z = 0; z < fieldDataList.size(); z++) {
                    JSONObject data = fieldDataList.getJSONObject(z);
                    String field = data.getString("field").split("\\.")[1];
                    String value = data.getString("value");
                    if (Objects.equals(value, "null")) {
                        value = "''";
                    } else if (!value.chars().allMatch(Character::isDigit)) {
                        value = "'" + value + "'";
                    }
                    childValue.set(insertFieldString.indexOf(field),value);
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
