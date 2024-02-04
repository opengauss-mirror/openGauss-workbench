/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.service;

import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.utils.response.RespBean;

/**
 * SqlOperation
 *
 * @author liu
 * @since 2023-09-17
 */
public interface Evaluate {
    /**
     * obtainMysqlByIP
     *
     * @param ip ip
     * @return RespBean
     */
    RespBean obtainMysqlByIP(String ip);

    /**
     * obtainOpenGaussByIP
     *
     * @param ip ip
     * @return RespBean
     */
    RespBean obtainOpenGaussByIP(String ip);

    /**
     * obtainMysqlIp
     *
     * @return RespBean
     */
    RespBean obtainMysqlIp();

    /**
     * obtainOpenGaussIp
     *
     * @return RespBean
     */
    RespBean openGaussIp();

    /**
     * saveAssessMent
     *
     * @param assessment assessment
     * @return int int
     */
    int saveAssessMent(Assessment assessment);

    /**
     * obtainAllEvaluationResults
     *
     * @param assessment assessment
     * @param pageNum pageNum
     * @param pageSize pageSize
     * @return RespBean
     */
    RespBean obtainAllEvaluationResults(Assessment assessment, int pageNum, int pageSize);

    /**
     * deleteAssess
     *
     * @param assessmentId assessmentId
     * @return RespBean
     */
    RespBean deleteAssess(Long assessmentId);
}
