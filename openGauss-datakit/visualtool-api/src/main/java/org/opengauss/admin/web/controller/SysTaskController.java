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
 * SysTaskController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysTaskController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.system.domain.SysTask;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task Controller
 *
 * @author xielibo
 * @date 2023-01-13
 */
@RestController
@RequestMapping("/sys/task")
public class SysTaskController extends BaseController {
    @Autowired
    private ISysTaskService sysTaskService;

    /**
     * page list
     */
    @PreAuthorize("@ss.hasPermi('system:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTaskDto task) {
        IPage<SysTask> list = sysTaskService.selectList(startPage(), task);
        return getDataTable(list);
    }

    /**
     * list
     */
    @PreAuthorize("@ss.hasPermi('system:task:list')")
    @GetMapping("/list/all")
    public AjaxResult listAll(SysTaskDto task) {
        List<SysTask> list = sysTaskService.selectListAll(task);
        return AjaxResult.success(list);
    }

    /**
     * getById
     */
    @PreAuthorize("@ss.hasPermi('system:task:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(sysTaskService.getDetailById(id));
    }

    /**
     * delete
     */
    @Log(title = "task", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('system:task:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        sysTaskService.deleteTask(ids);
        return AjaxResult.success();
    }

    /**
     * start
     */
    @Log(title = "task", businessType = BusinessType.START)
    @PreAuthorize("@ss.hasPermi('system:task:update')")
    @PostMapping("/start/{id}")
    public AjaxResult start(@PathVariable Integer id ) {
        sysTaskService.startTask(id);
        return AjaxResult.success();
    }

    /**
     * stop
     */
    @Log(title = "task", businessType = BusinessType.STOP)
    @PreAuthorize("@ss.hasPermi('system:task:update')")
    @PostMapping("/stop/{id}")
    public AjaxResult stop(@PathVariable Integer id ) {
        sysTaskService.stopTask(id);
        return AjaxResult.success();
    }

}
