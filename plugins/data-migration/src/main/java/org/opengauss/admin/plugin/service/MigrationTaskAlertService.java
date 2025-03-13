/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskAlert;
import org.opengauss.admin.plugin.dto.MigrationTaskAlertDto;

import java.util.List;

/**
 * tb migration alert service
 *
 * @since 2024/12/16
 */
public interface MigrationTaskAlertService extends IService<MigrationTaskAlert> {
    /**
     * refresh alert from portal
     *
     * @param task migration task entity
     */
    void refreshAlertByPortal(MigrationTask task);

    /**
     * select group alert page info
     *
     * @param page page info
     * @param taskId task id
     * @param migrationPhase migration phase id
     * @return page info
     */
    IPage<MigrationTaskAlertDto> selectGroupPage(IPage<MigrationTaskAlertDto> page, int taskId, int migrationPhase);

    /**
     * count alert numbers
     *
     * @param taskId task id
     * @return alert number
     */
    int countAlertByTaskId(int taskId);

    /**
     * count group alert numbers of each migration phase
     *
     * @param taskId task id
     * @return alert numbers of each migration phase
     */
    AjaxResult countGroupAlertNumber(int taskId);

    /**
     * delete alert by task id
     *
     * @param taskId task id
     */
    void deleteAlertByTaskId(int taskId);

    /**
     * delete alert by main task id
     *
     * @param mainTaskId main task id
     */
    void deleteByMainTaskId(int mainTaskId);

    /**
     * get group alert id list by alert entity
     *
     * @param alert alert entity
     * @return alert id list
     */
    List<Integer> getGroupAlertIds(MigrationTaskAlert alert);
}
