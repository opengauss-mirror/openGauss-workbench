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
 *  CollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/CollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service;

import com.nctigba.observability.sql.constant.CollectionTypeConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;

/**
 * CollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface CollectionItem<T> {
    /**
     * Collect data
     *
     * @param task Diagnosis task
     * @return Object
     */
    T collectData(DiagnosisTaskDO task);

    /**
     * Query data
     *
     * @param task Diagnosis task
     * @return Object
     */
    T queryData(DiagnosisTaskDO task);

    /**
     * Get collection type
     *
     * @return String
     */
    default String getCollectionType() {
        return CollectionTypeConstants.MIDDLE;
    }
}
