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

package org.opengauss.tun.controller;

import java.util.List;
import org.opengauss.tun.domain.ParameterRecommendation;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.domain.vo.ApplyVo;
import org.opengauss.tun.domain.vo.LogVo;
import org.opengauss.tun.domain.vo.TaskLogVo;
import org.opengauss.tun.service.ParameterRecommendationService;
import org.opengauss.tun.service.TuningLogService;
import org.opengauss.tun.service.TuningService;
import org.opengauss.tun.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * LinuxConfigController
 *
 * @author liu
 * @since 2023-12-01
 */
@RestController
@RequestMapping("/intelligent/tuning")
public class TuningController {
    @Autowired
    private TuningService tuningService;

    @Autowired
    private ParameterRecommendationService recommendationService;

    @Autowired
    private TuningLogService logService;

    /**
     * StartIntelligentTuning
     *
     * @param config config
     * @return RespBean
     */
    @PostMapping("/start")
    public RespBean startIntelligentTuning(@ModelAttribute TrainingConfig config) {
        return tuningService.startTuning(config);
    }

    /**
     * getFiles
     *
     * @param id id
     * @return RespBean
     */
    @GetMapping("/obtain/file/{id}")
    public RespBean getFiles(@PathVariable String id) {
        return tuningService.getFilesById(id);
    }

    /**
     * viewTaskExecutionLogs
     *
     * @param logVo logVo
     * @return RespBean
     */
    @PostMapping("/view/task/log")
    public RespBean viewTaskExecutionLogs(@RequestBody TaskLogVo logVo) {
        return tuningService.viewTaskExecutionLogs(logVo);
    }

    /**
     * downloadTaskExecutionLogs
     *
     * @param logVo    logVo
     * @param response response
     */
    @PostMapping("/download/task/log")
    public void downloadTaskExecutionLogs(@RequestBody TaskLogVo logVo, HttpServletResponse response) {
        tuningService.downloadTaskExecutionLogs(logVo, response);
    }

    /**
     * getCluster
     *
     * @return RespBean
     */
    @GetMapping("/obtain/cluster")
    public RespBean getCluster() {
        return tuningService.getCluster();
    }

    /**
     * getDatabase
     *
     * @param clusterName clusterName
     * @return RespBean
     */
    @GetMapping("/obtain/database")
    public RespBean getDatabase(@RequestParam String clusterName) {
        return tuningService.getDatabase(clusterName);
    }

    /**
     * getTaskclusterName
     *
     * @return RespBean
     */
    @GetMapping("/obtain/task/clusterName")
    public RespBean getTaskclusterName() {
        return tuningService.getTaskclusterName();
    }

    /**
     * getTaskDatabase
     *
     * @return RespBean
     */
    @GetMapping("/obtain/task/database")
    public RespBean getTaskDatabase() {
        return tuningService.getTaskDatabase();
    }

    /**
     * getAllTuning
     *
     * @param trainingConfig trainingConfig
     * @param pageNum        pageNum
     * @param pageSize       pageSize
     * @return RespBean
     */
    @PostMapping("/all/config/{pageNum}/{pageSize}")
    public RespBean getAllTuning(@ModelAttribute TrainingConfig trainingConfig, @PathVariable int pageNum,
                                 @PathVariable int pageSize) {
        return tuningService.getAllTuning(trainingConfig, pageNum, pageSize);
    }

    /**
     * deleteTask
     *
     * @param ids ids
     * @return RespBean
     */
    @PostMapping("/delete")
    public RespBean deleteTask(@RequestBody List<String> ids) {
        return tuningService.deleteTasks(ids);
    }

    /**
     * deleteSuggest
     *
     * @param ids ids
     * @return RespBean
     */
    @PostMapping("/delete/suggest")
    public RespBean deleteSuggest(@RequestBody List<String> ids) {
        return recommendationService.deleteSuggests(ids);
    }

    /**
     * deleteSuggest
     *
     * @param ids ids
     * @return RespBean
     */
    @PostMapping("/delete/log")
    public RespBean deleteLog(@RequestBody List<String> ids) {
        return logService.deleteLogs(ids);
    }

    /**
     * stopTask
     *
     * @param ids ids
     * @return RespBean
     */
    @PostMapping("/stop")
    public RespBean stopTask(@RequestBody List<String> ids) {
        return tuningService.stopTask(ids);
    }

    /**
     * getTuningParm
     *
     * @param trainingId trainingId
     * @return RespBean
     */
    @GetMapping("/obtain/parameter/list")
    public RespBean getTuningParm(@RequestParam Long trainingId) {
        return tuningService.getTuningParm(trainingId);
    }

    /**
     * getParameterRecommendation
     *
     * @param recommendation recommendation
     * @param pageNum        pageNum
     * @param pageSize       pageSize
     * @return RespBean
     */
    @PostMapping("/obtain/parameter/recommendation/{pageNum}/{pageSize}")
    public RespBean getParameterRecommendation(@RequestBody ParameterRecommendation recommendation,
                                               @PathVariable int pageNum, @PathVariable int pageSize) {
        return tuningService.getParameterRecommendation(recommendation, pageNum, pageSize);
    }

    /**
     * getrRecommendationClusterName
     *
     * @return RespBean
     */
    @GetMapping("/obtain/parameter/recommendation/clusterName")
    public RespBean getrRecommendationClusterName() {
        return tuningService.getrRecommendationClusterName();
    }

    /**
     * getrRecommendationDatabase
     *
     * @return RespBean
     */
    @GetMapping("/obtain/parameter/recommendation/database")
    public RespBean getrRecommendationDatabase() {
        return tuningService.getrRecommendationDatabase();
    }

    /**
     * getParameterView
     *
     * @param parameterId parameterId
     * @return RespBean
     */
    @GetMapping("/obtain/parameter/view")
    public RespBean getParameterView(@RequestParam String parameterId) {
        return tuningService.getParameterView(parameterId);
    }

    /**
     * getOperationLog
     *
     * @param logVo    logVo
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @return RespBean
     */
    @PostMapping("/obtain/operation/log/{pageNum}/{pageSize}")
    public RespBean getOperationLog(@RequestBody LogVo logVo, @PathVariable int pageNum, @PathVariable int pageSize) {
        return tuningService.getOperationLog(logVo, pageNum, pageSize);
    }

    /**
     * getOperationClusterName
     *
     * @return RespBean
     */
    @GetMapping("/obtain/operation/log/clusterName")
    public RespBean getOperationClusterName() {
        return tuningService.getOperationClusterName();
    }

    /**
     * applyToreduction
     *
     * @param applyVo applyVo
     * @return RespBean
     */
    @PostMapping("/apply/reduction")
    public RespBean applyToreduction(@RequestBody ApplyVo applyVo) {
        return tuningService.apply(applyVo);
    }

    /**
     * download
     *
     * @param trainingId trainingId
     * @param type       type
     * @param response   response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(@RequestParam String trainingId, @RequestParam String type, HttpServletResponse response) {
        tuningService.downloadLog(trainingId, type, response);
    }

    /**
     * preview
     *
     * @param trainingId trainingId
     * @param type       type
     * @return RespBean
     */
    @RequestMapping(value = "/preview", method = RequestMethod.GET)
    public RespBean preview(@RequestParam String trainingId, @RequestParam String type) {
        return tuningService.preview(trainingId, type);
    }
}
