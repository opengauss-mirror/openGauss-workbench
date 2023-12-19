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
 *  ThresholdService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/ThresholdService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service;

import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.query.ThresholdQuery;

import java.util.HashMap;
import java.util.List;

/**
 * ThresholdService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface ThresholdService {
    /**
     * Select all threshold
     *
     * @param diagnosisType Diagnosis type
     * @return List
     */
    List<DiagnosisThresholdDO> select(String diagnosisType);

    /**
     * Insert or update threshold
     *
     * @param thresholdQuery Threshold data
     */
    void insertOrUpdate(ThresholdQuery thresholdQuery);

    /**
     * Delete threshold
     *
     * @param id Primary key
     */
    void delete(int id);

    /**
     * Obtain the threshold value by the threshold name
     *
     * @param list Threshold name
     * @return HashMap
     */
    HashMap<String, String> getThresholdValue(List<String> list);
}
