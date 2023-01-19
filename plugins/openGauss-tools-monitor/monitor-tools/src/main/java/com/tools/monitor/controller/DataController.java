package com.tools.monitor.controller;

import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.OperatorType;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.service.MonitorService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping(value = "/create/config", method = RequestMethod.POST)
    @Log(title = "monitor-tools-config", businessType = BusinessType.INSERT, operatorType = OperatorType.PLUGIN)
    public ResponseVO dataSourceConfig(@Validated @RequestBody SysConfig sysConfig) {
        return monitorService.setSourceConfig(sysConfig);
    }

    @RequestMapping(value = "/update/config", method = RequestMethod.POST)
    @Log(title = "monitor-tools-config", businessType = BusinessType.UPDATE, operatorType = OperatorType.PLUGIN)
    public ResponseVO dataUpdateConfig(@Validated @RequestBody SysConfig sysConfig) {
        return monitorService.updateConfig(sysConfig);
    }

    @RequestMapping(value = "/list/source/{page}/{size}", method = RequestMethod.GET)
    public ResponseVO sourceList(@PathVariable Integer page, @PathVariable Integer size) {
        return monitorService.getDataSourceList(page, size);
    }

    @RequestMapping(value = "/source/name", method = RequestMethod.GET)
    public ResponseVO selectSource() {
        return monitorService.getDataSourceNameList();
    }

    @PostMapping("/delete")
    @Log(title = "monitor-tools-config", businessType = BusinessType.DELETE, operatorType = OperatorType.PLUGIN)
    public ResponseVO remove(@RequestBody List<Long> ids) throws SchedulerException, TaskException {
        return monitorService.deleteConfigById(ids);
    }

    @GetMapping("/zabbix/config")
    public ResponseVO getZabbixConfig(){
        return monitorService.selectZabbixConfig();
    }

    @GetMapping("/nagios/config")
    public ResponseVO getNagiosConfig(){
        return monitorService.selectNagiosConfig();
    }
}
