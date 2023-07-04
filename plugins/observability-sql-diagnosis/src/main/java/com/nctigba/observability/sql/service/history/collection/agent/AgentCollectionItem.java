/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.agent;

import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.util.AgentUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AgentCollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
public abstract class AgentCollectionItem implements CollectionItem<Object> {
    @Autowired
    AgentUtil util;

    public Object collectData(HisDiagnosisTask task) {
        return query(task);
    }

    public Object queryData(HisDiagnosisTask task) {
        return query(task);
    }

    abstract String getHttpParam();

    private Object query(HisDiagnosisTask task) {
        return util.rangQuery(task.getNodeId(), getHttpParam());
    }
}
