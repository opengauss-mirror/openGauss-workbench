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
 *  EbpfCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/ebpf/EbpfCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.ebpf;

import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.util.EbpfUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EbpfCollectionItem
 *
 * @author luomeng
 * @since 2023/7/31
 */
public abstract class EbpfCollectionItem implements CollectionItem<Object> {
    @Autowired
    private EbpfUtils util;

    /**
     * Collect ebpf data
     *
     * @param task task info
     * @return object data
     */
    public Object collectData(DiagnosisTaskDO task) {
        return util.callMonitor(task, getHttpParam());
    }

    /**
     * Query ebpf data
     *
     * @param task task info
     * @return object data
     */
    public Object queryData(DiagnosisTaskDO task) {
        return "ebpf";
    }

    /**
     * Get http param
     *
     * @return string
     */
    public abstract String getHttpParam();
}
