/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.ebpf;

import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.util.EbpfUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EbpfCollectionItem
 *
 * @author luomeng
 * @since 2023/7/31
 */
public abstract class EbpfCollectionItem implements CollectionItem<Object> {
    @Autowired
    private EbpfUtil util;

    /**
     * Collect ebpf data
     *
     * @param task task info
     * @return object data
     */
    public Object collectData(HisDiagnosisTask task) {
        return util.callMonitor(task, getHttpParam());
    }

    /**
     * Query ebpf data
     *
     * @param task task info
     * @return object data
     */
    public Object queryData(HisDiagnosisTask task) {
        return "ebpf";
    }

    /**
     * Get http param
     *
     * @return string
     */
    public abstract String getHttpParam();
}
