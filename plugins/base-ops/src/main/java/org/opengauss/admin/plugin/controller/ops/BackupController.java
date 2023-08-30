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
 * BackupController.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/BackupController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.service.ops.IOpsBackupService;
import org.opengauss.admin.plugin.vo.ops.BackupInputDto;
import org.opengauss.admin.plugin.vo.ops.BackupVO;
import org.opengauss.admin.plugin.vo.ops.RecoverInputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author lhf
 * @date 2022/11/5 09:49
 **/
@RestController
@RequestMapping("/backup")
public class BackupController extends BaseController {
    @Autowired
    private IOpsBackupService backupService;

    @PostMapping("/backup")
    public AjaxResult backup(@RequestBody @Validated BackupInputDto backup) {
        backupService.backup(backup);
        return AjaxResult.success();
    }

    @GetMapping("/page")
    public AjaxResult pageBackup(@RequestParam(required = false, value = "clusterId") String clusterId) {
        Page<BackupVO> page = backupService.pageBackup(startPage(), clusterId);
        return AjaxResult.success(page);
    }

    @PostMapping("/recover/{id}")
    public AjaxResult recover(@PathVariable("id") String id, @RequestBody RecoverInputDto recover) {
        backupService.recover(id, recover);
        return AjaxResult.success();
    }

    @DeleteMapping("/del/{id}")
    public AjaxResult del(@PathVariable("id") String id) {
        backupService.del(id);
        return AjaxResult.success();
    }
}
