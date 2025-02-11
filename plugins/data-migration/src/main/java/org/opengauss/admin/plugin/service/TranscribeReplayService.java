/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.FailSqlModel;
import org.opengauss.admin.plugin.domain.SqlDuration;
import org.opengauss.admin.plugin.domain.TranscribeReplayHost;
import org.opengauss.admin.plugin.domain.TranscribeReplaySlowSql;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskQueryDto;
import org.opengauss.admin.plugin.vo.ShellInfoVo;

import java.util.List;

/**
 * TranscribeReplayService
 *
 * @since 2025-02-10
 */

public interface TranscribeReplayService extends IService<TranscribeReplayTask> {
    /**
     * saveTask
     *
     * @param taskDto taskDto
     * @return Integer
     */
    Integer saveTask(TranscribeReplayTaskDto taskDto);

    /**
     * startTask
     *
     * @param id id
     * @return AjaxResult
     */
    AjaxResult startTask(Integer id);

    /**
     * finishTask
     *
     * @param id id
     */
    void finishTask(Integer id);

    /**
     * deleteTask
     *
     * @param ids ids
     */
    void deleteTask(Integer[] ids);

    /**
     * selectList
     *
     * @param startPage startPage
     * @param taskQueryDto taskQueryDto
     * @return IPage
     */
    IPage<TranscribeReplayTask> selectList(Page startPage, TranscribeReplayTaskQueryDto taskQueryDto);

    /**
     * getSlowSql
     *
     * @param startPage startPage
     * @param id id
     * @param sql sql
     * @return IPage
     */
    IPage<TranscribeReplaySlowSql> getSlowSql(Page startPage, Integer id, String sql);

    /**
     * getFailSql
     *
     * @param startPage startPage
     * @param id id
     * @param sql sql
     * @return IPage
     */
    IPage<FailSqlModel> getFailSql(Page startPage, Integer id, String sql);

    /**
     * getShellInfo
     *
     * @param taskId taskId
     * @param runHostType runHostType
     * @return ShellInfoVo
     */
    ShellInfoVo getShellInfo(int taskId, String runHostType);

    /**
     * getHostInfo
     *
     * @param taskId taskId
     * @return List
     */
    List<TranscribeReplayHost> getHostInfo(int taskId);

    /**
     * getSqlDuration
     *
     * @param taskId taskId
     * @return SqlDuration
     */
    SqlDuration getSqlDuration(Integer taskId);
}
