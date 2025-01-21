/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 * MigrationTaskCheckProgressService.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskCheckProgressService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.plugin.context.MigrationTaskContext;
import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressDetail;
import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressSummary;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressSummaryService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;

/**
 * MigrationTaskCheckProgressService
 *
 * @author: wangchao
 * @since: 2024/12/30 09:53
 **/
@Service
@Slf4j
public class MigrationTaskCheckProgressService {
    private static final Map<Integer, MigrationTaskCheckProgressMonitor> MONITOR_TASK = new ConcurrentHashMap<>();
    private static final Map<Integer, Future<?>> MONITOR_TASK_FUTURE = new ConcurrentHashMap<>();
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int DEFAULT_PAGE_NUM = 1;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private MigrationTaskCheckProgressSummaryService migrationTaskCheckProgressSummaryService;
    @Resource
    private MigrationTaskCheckProgressDetailService migrationTaskCheckProgressDetailService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * refreshCheckProgress
     *
     * @param taskId task id
     * @param execStatus exec status
     */
    public void refreshCheckProgress(Integer taskId, Integer execStatus) {
        if (MONITOR_TASK.containsKey(taskId)) {
            MigrationTaskCheckProgressMonitor monitor = MONITOR_TASK.get(taskId);
            monitor.refresh(execStatus);
            if (monitor.isCheckTaskCompleted()) {
                stopByTaskIds(List.of(taskId));
                MONITOR_TASK.remove(taskId);
                MONITOR_TASK_FUTURE.remove(taskId);
            }
        }
    }

    /**
     * startCheckProgress
     *
     * @param context context
     */
    public void startCheckProgress(MigrationTaskContext context) {
        Integer id = context.getId();
        if (MONITOR_TASK.containsKey(id)) {
            return;
        }
        context.setEncryptionUtils(encryptionUtils);
        context.setMigrationTaskCheckProgressDetailService(migrationTaskCheckProgressDetailService);
        context.setMigrationTaskCheckProgressSummaryService(migrationTaskCheckProgressSummaryService);
        MigrationTaskCheckProgressMonitor taskMonitor = new MigrationTaskCheckProgressMonitor(context);
        MONITOR_TASK.put(id, taskMonitor);
        MONITOR_TASK_FUTURE.put(id, threadPoolTaskExecutor.submit(taskMonitor));
    }

    /**
     * query check progress summary
     *
     * @param id task id
     * @return summary
     */
    public MigrationTaskCheckProgressSummary queryFullCheckSummaryOfMigrationTask(Integer id) {
        return migrationTaskCheckProgressSummaryService.queryFullCheckSummaryOfMigrationTask(id);
    }

    /**
     * page check progress detail
     *
     * @param id task id
     * @param status status
     * @param pageSize page size
     * @param pageNum page num
     * @return page
     */
    public IPage<MigrationTaskCheckProgressDetail> pageFullCheckDetailOfMigrationTask(Integer id, String status,
        int pageSize, int pageNum) {
        int pageSizeVal = Optional.of(pageSize).filter(size -> size >= DEFAULT_PAGE_SIZE).orElse(DEFAULT_PAGE_SIZE);
        int pageNumVal = Optional.of(pageNum).filter(num -> num >= DEFAULT_PAGE_NUM).orElse(DEFAULT_PAGE_NUM);
        LambdaQueryWrapper<MigrationTaskCheckProgressDetail> queryWrapper = Wrappers.lambdaQuery(
            MigrationTaskCheckProgressDetail.class);
        queryWrapper.eq(MigrationTaskCheckProgressDetail::getTaskId, id);
        queryWrapper.eq(StrUtil.isNotEmpty(status), MigrationTaskCheckProgressDetail::getStatus, status);
        queryWrapper.orderByDesc(MigrationTaskCheckProgressDetail::getCreateTime);
        Page<MigrationTaskCheckProgressDetail> page = new Page<>(pageNumVal, pageSizeVal);
        page.setCurrent(pageNumVal);
        return migrationTaskCheckProgressDetailService.page(page, queryWrapper);
    }

    /**
     * stop migration task check monitor
     *
     * @param ids task ids
     */
    public void stopByTaskIds(List<Integer> ids) {
        ids.forEach(id -> {
            if (MONITOR_TASK.containsKey(id)) {
                MigrationTaskCheckProgressMonitor monitor = MONITOR_TASK.get(id);
                monitor.stop();
                if (MONITOR_TASK_FUTURE.containsKey(id)) {
                    Future<?> future = MONITOR_TASK_FUTURE.get(id);
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.warn("stop monitor task has interrupted or execution id: {} {}", id, e.getMessage());
                    }
                    MONITOR_TASK_FUTURE.remove(id);
                }
                MONITOR_TASK.remove(id);
            }
        });
    }

    /**
     * delete migration task check summary and details
     *
     * @param ids ids
     */
    public void deleteByTaskIds(List<Integer> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            stopByTaskIds(ids);
            migrationTaskCheckProgressSummaryService.removeByTaskIds(ids);
            migrationTaskCheckProgressDetailService.removeByTaskIds(ids);
        }
    }
}
