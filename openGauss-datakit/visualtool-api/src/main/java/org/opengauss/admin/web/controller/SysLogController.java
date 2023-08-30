/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * SysLogController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysLogController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.SysLogConfigDto;
import org.opengauss.admin.common.core.vo.SysLogConfigVo;
import org.opengauss.admin.system.service.ISysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * System log controller
 *
 * @author snow
 */
@Slf4j
@RestController
@RequestMapping("/system/log")
public class SysLogController {

    @Autowired
    ISysLogService sysLogService;

    /**
     * List all configurable system log config
     * @return Config
     */
    @RequestMapping
    public AjaxResult getAllLogConfig() {
        SysLogConfigVo sysLogConfigVo = sysLogService.getAllLogConfig();
        return AjaxResult.success(sysLogConfigVo);
    }

    /**
     * Save system log config
     * @param sysLogConfigDto
     * @return
     */
    @PostMapping
    public AjaxResult saveAllLogConfig(@RequestBody SysLogConfigDto sysLogConfigDto) {
        sysLogService.saveAllLogConfig(sysLogConfigDto);
        return AjaxResult.success();
    }

    /**
     * List all saved log file
     * @return
     */
    @GetMapping("files")
    public AjaxResult files() {
        List<Map<String, Object>> files = sysLogService.listAllLogFile();
        return AjaxResult.success(files);
    }

    /**
     * Download single log file by filename
     * @param filename the log filename
     * @return
     */
    @GetMapping("download")
    public ResponseEntity<ByteArrayResource> download(String filename) throws Exception {
        File logfile = sysLogService.getLogFileByName(filename);
        byte[] bytes = Files.readAllBytes(logfile.toPath());
        ByteArrayResource bar = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=" + filename)
                .body(bar);
    }

    @GetMapping("print")
    public AjaxResult testPrint() {
        log.trace("Trace log");
        log.debug("Debug log");
        log.info("Info log");
        log.warn("Warn log");
        log.error("Error Log");
        return AjaxResult.success();
    }
}
