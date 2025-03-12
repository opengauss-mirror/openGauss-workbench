/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.dto.MigrationTaskAlertDto;
import org.opengauss.admin.plugin.service.MigrationTaskAlertDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * migration task alert controller
 *
 * @since 2024/12/17
 */
@Slf4j
@RestController
@RequestMapping("/migration/alert")
public class MigrationTaskAlertController extends BaseController {
    @Autowired
    private MigrationTaskAlertService alertService;
    @Autowired
    private MigrationTaskAlertDetailService alertDetailService;

    /**
     * get alert log page info
     *
     * @param taskId task id
     * @param migrationPhase migration phase id
     * @return page info
     */
    @GetMapping("/list/{taskId}/{migrationPhase}")
    public TableDataInfo getList(@PathVariable int taskId, @PathVariable int migrationPhase) {
        IPage<MigrationTaskAlertDto> iPage = alertService.selectGroupPage(startPage(), taskId, migrationPhase);
        return getDataTable(iPage);
    }

    /**
     * count alert number
     *
     * @param taskId task id
     * @return alert numbers of each migration phase
     */
    @GetMapping("/count/{taskId}")
    public AjaxResult countAlertNumber(@PathVariable int taskId) {
        return alertService.countGroupAlertNumber(taskId);
    }

    /**
     * get alert detail
     *
     * @param id alert id
     * @return alert detail
     */
    @GetMapping("/detail/{id}")
    public AjaxResult getDetail(@PathVariable int id) {
        return AjaxResult.success(alertDetailService.getGroupDetailByAlertId(id));
    }
}
