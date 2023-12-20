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
 *  DiagnosisService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/DiagnosisService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service;

import com.nctigba.observability.sql.model.dto.TreeNodeDTO;

/**
 * DiagnosisService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface DiagnosisService {
    /**
     * Get diagnosis tree
     *
     * @param taskId task id
     * @param isAll is query all
     * @param diagnosisType diagnosis type
     * @return TreeNodeDTO
     */
    TreeNodeDTO getTopologyMap(int taskId, boolean isAll, String diagnosisType);

    /**
     * Get diagnosis point detail
     *
     * @param taskId task id
     * @param pointName diagnosis point name
     * @param diagnosisType diagnosis type
     * @return Object
     */
    Object getNodeDetail(int taskId, String pointName, String diagnosisType);
}
