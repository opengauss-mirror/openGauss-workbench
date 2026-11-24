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

package com.tools.monitor.controller;

import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.service.MonitorService;
import java.util.List;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * DataController
 *
 * @author liu
 * @since 2022-10-01
 */
@RestController
@RequestMapping("/data")
public class DataController {
    @Autowired
    private MonitorService monitorService;

    /**
     * dataSourceConfig
     *
     * @param sysConfig sysConfig
     * @return ResponseVO
     */
    @RequestMapping(value = "/create/config", method = RequestMethod.POST)
    public ResponseVO dataSourceConfig(@Validated @RequestBody SysConfig sysConfig) {
        return monitorService.setSourceConfig(sysConfig);
    }

    /**
     * dataUpdateConfig
     *
     * @param sysConfig sysConfig
     * @return ResponseVO
     */
    @RequestMapping(value = "/update/config", method = RequestMethod.POST)
    public ResponseVO dataUpdateConfig(@Validated @RequestBody SysConfig sysConfig) {
        return monitorService.updateConfig(sysConfig);
    }

    /**
     * sourceList
     *
     * @param page page
     * @param size size
     * @return ResponseVO
     */
    @RequestMapping(value = "/list/source/{page}/{size}", method = RequestMethod.GET)
    public ResponseVO sourceList(@PathVariable Integer page, @PathVariable Integer size) {
        return monitorService.getDataSourceList(page, size);
    }

    /**
     * selectSource
     *
     * @return ResponseVO
     */
    @RequestMapping(value = "/source/name", method = RequestMethod.GET)
    public ResponseVO selectSource() {
        return monitorService.getDataSourceNameList();
    }

    /**
     * remove
     *
     * @param ids ids
     * @return ResponseVO
     * @throws SchedulerException SchedulerException
     * @throws TaskException      TaskException
     */
    @PostMapping("/delete")
    public ResponseVO remove(@RequestBody List<Long> ids) throws SchedulerException, TaskException {
        return monitorService.deleteConfigById(ids);
    }

    /**
     * getZabbixConfig
     *
     * @return ResponseVO
     */
    @GetMapping("/zabbix/config")
    public ResponseVO getZabbixConfig() {
        return monitorService.selectZabbixConfig();
    }

    /**
     * getNagiosConfig
     *
     * @return ResponseVO
     */
    @GetMapping("/nagios/config")
    public ResponseVO getNagiosConfig() {
        return monitorService.selectNagiosConfig();
    }
}
