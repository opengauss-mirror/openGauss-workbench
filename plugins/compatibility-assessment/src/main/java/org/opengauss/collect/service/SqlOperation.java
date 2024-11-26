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

import java.util.Optional;

import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.utils.response.RespBean;

import javax.servlet.http.HttpServletResponse;

/**
 * SqlOperation
 *
 * @author liu
 * @since 2023-09-17
 */
public interface SqlOperation {
    /**
     * downloadChrome
     *
     * @param host host
     * @param hostUser hostUser
     * @param filePath filePath
     * @param response response
     */
    void downloadChrome(String host, String hostUser, String filePath, HttpServletResponse response);

    /**
     * startCollectingSql
     *
     * @param period period
     * @return RespBean RespBean
     */
    RespBean startCollectingSql(CollectPeriod period);

    /**
     * getLinuxConfig
     *
     * @param host host
     * @param hostUser hostUser
     * @return LinuxConfig LinuxConfig
     */
    Optional<LinuxConfig> getLinuxConfig(String host, String hostUser);

    /**
     * startAssessmentSql
     *
     * @param assessment assessment
     * @param sqlInputType sqlInputType
     * @param userId userId
     * @return RespBean
     */
    RespBean startAssessmentSql(Assessment assessment, String sqlInputType, Integer userId);

    /**
     * downloadAssessmentSql
     *
     * @param reportFileName reportFileName
     * @param response response
     */
    void downloadAssessmentSql(String reportFileName, HttpServletResponse response);

    /**
     * sqlAssessInit
     *
     * @return RespBean RespBean
     */
    RespBean sqlAssessInit();

    /**
     * getAllPids
     *
     * @param host host
     * @param hostUser hostUser
     * @return RespBean RespBean
     */
    RespBean getAllPids(String host, String hostUser);

    /**
     * getAllPids
     *
     * @param userId userId
     * @return RespBean
     */
    RespBean getUploadPath(Integer userId);
}
