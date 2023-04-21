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
 * SyncController.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/controller/SyncController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.controller;

import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.OperatorType;
import org.opengauss.admin.plugin.bean.SyncConfigDto;
import org.opengauss.admin.plugin.bean.SyncConstants;
import org.opengauss.admin.plugin.service.SyncDataServiceImpl;
import org.opengauss.admin.plugin.util.LocalCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: SyncController
 * @description: resetapi for sync
 * @author: xielibo
 * @date: 2022-10-28 12:50
 **/
@Slf4j
@RestController
@RequestMapping("/sync")
public class SyncController {

    @Autowired
    private SyncDataServiceImpl syncDataService;

    @GetMapping("/getCurSyncStatus")
    public AjaxResult getCurSyncStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", 0);
        Object o = LocalCache.get(SyncConstants.CACHE_KEY_SYNC_PARAMS);
        if (o != null) {
            SyncConfigDto configDto = (SyncConfigDto) o;
            boolean isComplete = syncDataService.checkSyncSuccess(configDto);
            if (!isComplete) {
                result.put("status", 1);
                result.put("config", configDto);
            }
        }
        return AjaxResult.success(result);
    }

    @PostMapping("/config")
    @Log(title = "sync-mysql", businessType = BusinessType.OTHER, operatorType = OperatorType.PLUGIN)
    public AjaxResult config(@RequestBody SyncConfigDto configDto) {
        boolean flag = syncDataService.syncHandler(configDto);
        return AjaxResult.success(flag);
    }
}
