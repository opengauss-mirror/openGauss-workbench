/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AgentCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/agent/AgentCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.agent;

import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.util.AgentUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AgentCollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
public abstract class AgentCollectionItem implements CollectionItem<Object> {
    @Autowired
    AgentUtils util;

    public Object collectData(DiagnosisTaskDO task) {
        return query(task);
    }

    public Object queryData(DiagnosisTaskDO task) {
        return query(task);
    }

    /**
     * Get http param
     *
     * @return String
     */
    public abstract String getHttpParam();

    private Object query(DiagnosisTaskDO task) {
        return util.rangQuery(task.getNodeId(), getHttpParam());
    }
}
