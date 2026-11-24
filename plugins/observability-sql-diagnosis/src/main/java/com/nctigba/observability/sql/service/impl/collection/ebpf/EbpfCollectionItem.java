/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
