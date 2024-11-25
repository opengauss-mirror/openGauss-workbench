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

package org.opengauss.collect.web.controller;

import java.util.List;

import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.service.SqlOperation;
import org.opengauss.collect.service.SqlTaskService;
import org.opengauss.collect.utils.response.RespBean;
import org.opengauss.admin.common.core.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * SqlOperationController
 *
 * @author liu
 * @since 2022-10-01
 */
@RestController
@RequestMapping("/operate")
public class SqlOperationController extends BaseController {
    @Autowired
    private SqlOperation operation;

    @Autowired
    private SqlTaskService taskService;

    /**
     * saveConfig
     *
     * @param collectPeriod collectPeriod
     * @return RespBean
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public RespBean saveConfig(@ModelAttribute CollectPeriod collectPeriod) {
        return taskService.saveTask(collectPeriod);
    }

    /**
     * updateConfig
     *
     * @param collectPeriod collectPeriod
     * @return RespBean
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RespBean updateConfig(@ModelAttribute CollectPeriod collectPeriod) {
        return taskService.updateTask(collectPeriod);
    }

    /**
     * deleteConfig
     *
     * @param ids ids
     * @return RespBean
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public RespBean deleteConfig(@RequestBody List<Long> ids) {
        return taskService.deleteTask(ids);
    }

    /**
     * startCollect
     *
     * @param collectPeriod collectPeriod
     * @return RespBean
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public RespBean startCollect(@RequestBody CollectPeriod collectPeriod) {
        return taskService.getTaskList(collectPeriod);
    }

    /**
     * downloadChrome
     *
     * @param host host
     * @param hostUser hostUser
     * @param filePath filePath
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadChrome(@RequestParam String host, @RequestParam String hostUser, @RequestParam String filePath,
        HttpServletResponse response) {
        operation.downloadChrome(host, hostUser, filePath, response);
    }

    /**
     * checkName
     *
     * @param name name
     * @return RespBean
     */
    @RequestMapping(value = "/hasName", method = RequestMethod.GET)
    public RespBean checkName(@RequestParam String name) {
        return taskService.checkName(name);
    }

    /**
     * getIps
     *
     * @return RespBean
     */
    @RequestMapping(value = "/all/ip", method = RequestMethod.GET)
    public RespBean getIps() {
        return taskService.getIps();
    }

    /**
     * getProcess
     *
     * @return RespBean
     */
    @RequestMapping(value = "/complete/process", method = RequestMethod.GET)
    public RespBean getProcess() {
        return taskService.getCompleteProcess();
    }

    /**
     * getPids
     *
     * @param host host
     * @param hostUser hostUser
     * @return RespBean RespBean
     */
    @RequestMapping(value = "/all/pids", method = RequestMethod.GET)
    public RespBean getPids(@RequestParam String host, @RequestParam String hostUser) {
        return operation.getAllPids(host, hostUser);
    }

    /**
     * sqlCollectStart
     *
     * @param period period
     * @return RespBean
     */
    @RequestMapping(value = "/collect/start", method = RequestMethod.POST)
    public RespBean sqlCollectStart(@RequestBody CollectPeriod period) {
        return operation.startCollectingSql(period);
    }

    /**
     * sqlAssessStart
     *
     * @param assessment assessment
     * @param sqlInputType sqlInputType
     * @return RespBean
     */
    @RequestMapping(value = "/assess/start", method = RequestMethod.POST)
    public RespBean sqlAssessStart(@ModelAttribute Assessment assessment, @RequestParam String sqlInputType) {
        return operation.startAssessmentSql(assessment, sqlInputType, getUserId());
    }

    /**
     * sqlAssessInit
     *
     * @return RespBean
     */
    @RequestMapping(value = "/assess/init", method = RequestMethod.GET)
    public RespBean sqlAssessInit() {
        return operation.sqlAssessInit();
    }

    /**
     * sqlAssessDownload
     *
     * @param reportFileName reportFileName
     * @param response response
     */
    @RequestMapping(value = "/assess/download", method = RequestMethod.GET)
    public void sqlAssessDownload(@RequestParam String reportFileName, HttpServletResponse response) {
        operation.downloadAssessmentSql(reportFileName, response);
    }

    /**
     * getSystemPath
     *
     * @return RespBean
     */
    @RequestMapping(value = "/assess/systemPath", method = RequestMethod.GET)
    public RespBean getSystemPath() {
        return operation.getUploadPath(getUserId());
    }
}
