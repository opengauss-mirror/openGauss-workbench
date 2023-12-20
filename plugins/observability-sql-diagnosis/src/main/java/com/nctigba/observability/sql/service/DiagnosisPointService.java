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
 *  DiagnosisPointService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/DiagnosisPointService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service;

import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;

import java.util.List;

/**
 * DiagnosisPointService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface DiagnosisPointService<T> {
    /**
     * Options to get diagnostic points
     *
     * @return List
     */
    List<String> getOption();

    /**
     * Obtain the collection items of diagnostic points
     *
     * @return List
     */
    List<CollectionItem<?>> getSourceDataKeys();

    /**
     * Diagnostic point diagnostic analysis data
     *
     * @param task Diagnosis task
     * @param dataStoreService Stored Data
     * @return AnalysisDTO
     */
    AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService);

    /**
     * Get diagnosis point data
     *
     * @param taskId Task id
     * @return Object
     */
    T getShowData(int taskId);

    /**
     * Get diagnosis type
     *
     * @return String
     */
    default String getDiagnosisType() {
        return PointTypeConstants.HISTORY;
    }
}
