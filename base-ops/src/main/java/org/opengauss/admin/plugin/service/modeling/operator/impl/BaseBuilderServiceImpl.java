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
 * BaseBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/BaseBuilderServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.operator.impl;


import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowOperatorBuilderService;

/**
 * @author songlei
 * @date 2022/8/25 10:08
 */
public class BaseBuilderServiceImpl implements IModelingDataFlowOperatorBuilderService {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject) {
        return sqlObject;
    }

    public String formatField(String field) {
        if (field.contains(".")) {
            return "\"" + field.split("\\.")[0] + "\"." + field.split("\\.")[1];
        }
        return field;
    }

    public String pureField(String field) {
        if (field.contains(".")) {
            return field.split("\\.")[1];
        }
        return field;
    }

    public String pureAliasField(String field) {
        return pureField(field).replace("[as]","");
    }
}
