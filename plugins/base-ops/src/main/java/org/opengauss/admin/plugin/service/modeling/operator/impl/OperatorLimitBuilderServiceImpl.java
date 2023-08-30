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
 * OperatorLimitBuilderServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/impl/OperatorLimitBuilderServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

/**
 * @author songlei
 * @date 2022/8/16 9:46
 */
@Component("limit")
public class OperatorLimitBuilderServiceImpl extends BaseBuilderServiceImpl {
    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        //limit region modify
        String oriSql = sqlObject.getLimitRegion();

        JSONObject restriction = params.getJSONObject("restriction");

        if (!restriction.isEmpty()) {
            String limitCount = restriction.getString("limitCount");
            if (limitCount != null) {
                oriSql += "limit "+ limitCount + " ";
            }

            String skip = restriction.getString("skip");
            if (skip != null) {
                oriSql += "offset " + skip + " ";
            }
        }

        sqlObject.setLimitRegion(oriSql);

        return sqlObject;
    }

}
