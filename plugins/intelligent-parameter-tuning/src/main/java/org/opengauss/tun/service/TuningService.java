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

package org.opengauss.tun.service;

import java.util.List;
import org.opengauss.tun.domain.ParameterRecommendation;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.domain.vo.ApplyVo;
import org.opengauss.tun.domain.vo.LogVo;
import org.opengauss.tun.domain.vo.TaskLogVo;
import org.opengauss.tun.utils.response.RespBean;

import javax.servlet.http.HttpServletResponse;

/**
 * TuningService
 *
 * @author liu
 * @since 2023-12-20
 */
public interface TuningService {
    /**
     * StartTuning
     *
     * @param trainingConfig trainingConfig
     * @return RespBean
     */
    RespBean startTuning(TrainingConfig trainingConfig);

    /**
     * getCluster
     *
     * @return RespBean
     */
    RespBean getCluster();

    /**
     * getDatabase
     *
     * @param clusterName clusterName
     * @return RespBean
     */
    RespBean getDatabase(String clusterName);

    /**
     * getAllTuning
     *
     * @param trainingConfig trainingConfig
     * @param pageNum        pageNum
     * @param pageSize       pageSize
     * @return RespBean
     */
    RespBean getAllTuning(TrainingConfig trainingConfig, int pageNum, int pageSize);

    /**
     * getTuningParm
     *
     * @param trainingId trainingId
     * @return RespBean
     */
    RespBean getTuningParm(Long trainingId);

    /**
     * selectCountById
     *
     * @param trainingId trainingId
     * @return int
     */
    int selectCountById(String trainingId);

    /**
     * getParameterRecommendation
     *
     * @param recommendation recommendation
     * @param pageNum        pageNum
     * @param pageSize       pageSize
     * @return RespBean
     */
    RespBean getParameterRecommendation(ParameterRecommendation recommendation, int pageNum, int pageSize);

    /**
     * getParameterView
     *
     * @param parameterId parameterId
     * @return RespBean
     */
    RespBean getParameterView(String parameterId);

    /**
     * getOperationLog
     *
     * @param logVo    logVo
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @return RespBean
     */
    RespBean getOperationLog(LogVo logVo, int pageNum, int pageSize);

    /**
     * apply
     *
     * @param applyVo applyVo
     * @return RespBean
     */
    RespBean apply(ApplyVo applyVo);

    /**
     * downloadLog
     *
     * @param trainingId trainingId
     * @param type       type
     * @param response   response
     */
    void downloadLog(String trainingId, String type, HttpServletResponse response);

    /**
     * getOperationClusterName
     *
     * @return RespBean
     */
    RespBean getOperationClusterName();

    /**
     * getrRecommendationDatabase
     *
     * @return RespBean
     */
    RespBean getrRecommendationDatabase();

    /**
     * getrRecommendationClusterName
     *
     * @return RespBean
     */
    RespBean getrRecommendationClusterName();

    /**
     * getTaskclusterName
     *
     * @return RespBean
     */
    RespBean getTaskclusterName();

    /**
     * getTaskDatabase
     *
     * @return RespBean
     */
    RespBean getTaskDatabase();

    /**
     * preview
     *
     * @param trainingId trainingId
     * @param type       type
     * @return String
     */
    RespBean preview(String trainingId, String type);

    /**
     * ViewTaskExecutionLogs
     *
     * @param logVo logVo
     * @return RespBean
     */
    RespBean viewTaskExecutionLogs(TaskLogVo logVo);

    /**
     * downloadTaskExecutionLogs
     *
     * @param logVo    logVo
     * @param response response
     */
    void downloadTaskExecutionLogs(TaskLogVo logVo, HttpServletResponse response);

    /**
     * getFilesById
     *
     * @param id id
     * @return RespBean
     */
    RespBean getFilesById(String id);

    /**
     * selectById
     *
     * @param id id
     * @return TrainingConfig
     */
    TrainingConfig selectById(String id);

    /**
     * deleteTasks
     *
     * @param ids ids
     * @return RespBean
     */
    RespBean deleteTasks(List<String> ids);

    /**
     * stopTask
     *
     * @param ids ids
     * @return RespBean
     */
    RespBean stopTask(List<String> ids);
}
