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

package org.opengauss.admin.plugin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.plugin.constants.TaskAlertConstants;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;
import org.opengauss.admin.plugin.domain.FailSqlModel;
import org.opengauss.admin.plugin.domain.ParamModel;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;
import org.opengauss.admin.plugin.enums.TranscribeReplayStatus;
import org.opengauss.admin.plugin.mapper.TranscribeReplayFailSqlMapper;
import org.opengauss.admin.plugin.mapper.TranscribeReplayParamMapper;
import org.opengauss.admin.plugin.service.TranscribeReplayFailSqlService;
import org.opengauss.admin.plugin.service.TranscribeReplayService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TranscribeReplayFailSqlServiceImpl
 *
 * @since 2025-02-10
 */

@Service
@Slf4j
public class TranscribeReplayFailSqlServiceImpl extends ServiceImpl<TranscribeReplayFailSqlMapper, FailSqlModel>
    implements TranscribeReplayFailSqlService {
    @Autowired
    private TranscribeReplayService transcribeReplayService;
    @Autowired
    private TranscribeReplayParamMapper transcribeReplayParamMapper;
    @Autowired
    private TranscribeReplayFailSqlMapper transcribeReplayFailSqlMapper;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private Cache<Integer, Integer> resolvedObjectsNumberCache;

    @Override
    public void syncRefreshFailSql(TranscribeReplayTask task) {
        Integer taskId = task.getId();
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable syncTask = () -> {
            if (TranscribeReplayStatus.RUNNING.getCode()
                .equals(transcribeReplayService.getById(task.getId()).getExecutionStatus())) {
                try {
                    log.debug("Sync refresh task for get fail sql , taskId:{}", taskId);
                    refreshFailSql(task);
                } catch (IOException e) {
                    log.error("Sync refresh task status error. message: {}", e.getMessage());
                }
            } else {
                scheduledExecutor.shutdown();
            }
        };
        scheduledExecutor.scheduleAtFixedRate(syncTask, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public IPage<FailSqlModel> selectList(Page startPage, Integer id, String sql) {
        LambdaQueryWrapper<FailSqlModel> queryWrapper = Wrappers.lambdaQuery(FailSqlModel.class);
        queryWrapper.eq(Objects.nonNull(id), FailSqlModel::getTaskId, id)
            .like(StrUtil.isNotEmpty(sql), FailSqlModel::getSql, sql);
        Page<FailSqlModel> page = page(startPage, queryWrapper);
        List<FailSqlModel> failSqlList = page.getRecords();
        if (failSqlList.isEmpty()) {
            return page;
        }
        List<Integer> failSqlIds = failSqlList.stream()
            .map(FailSqlModel::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        LambdaQueryWrapper<ParamModel> paramQueryWrapper = Wrappers.lambdaQuery(ParamModel.class);
        paramQueryWrapper.in(ParamModel::getFailSqlId, failSqlIds);
        List<ParamModel> paramModels = transcribeReplayParamMapper.selectList(paramQueryWrapper);
        Map<Integer, List<ParamModel>> paramModelMap = paramModels.stream()
            .collect(Collectors.groupingBy(ParamModel::getFailSqlId));
        for (FailSqlModel failSqlModel : failSqlList) {
            List<ParamModel> parameters = paramModelMap.getOrDefault(failSqlModel.getId(), Collections.emptyList());
            failSqlModel.setParameters(parameters);
        }

        return page;
    }

    @Override
    public List<FailSqlModel> getListByTaskId(Integer taskId) {
        LambdaQueryWrapper<FailSqlModel> queryWrapper = Wrappers.lambdaQuery(FailSqlModel.class);
        queryWrapper.eq(Objects.nonNull(taskId), FailSqlModel::getTaskId, taskId);
        return transcribeReplayFailSqlMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByTaskId(Integer taskId) {
        LambdaQueryWrapper<FailSqlModel> queryWrapper = Wrappers.lambdaQuery(FailSqlModel.class);
        queryWrapper.eq(Objects.nonNull(taskId), FailSqlModel::getTaskId, taskId);
        List<FailSqlModel> failSqlModels = transcribeReplayFailSqlMapper.selectList(queryWrapper);
        if (failSqlModels.size() > 0) {
            transcribeReplayFailSqlMapper.deleteBatchIds(failSqlModels);
        }
    }

    /**
     * refreshFailSql
     *
     * @param task task
     * @throws IOException IOException
     */
    public void refreshFailSql(TranscribeReplayTask task) throws IOException {
        Integer taskId = task.getId();
        Integer cacheNumber = resolvedObjectsNumberCache.asMap().get(taskId);
        int resolvedNumber = cacheNumber == null ? -1 : cacheNumber;
        if (resolvedNumber == -1) {
            resolvedNumber = countFailSqlByTaskId(taskId);
            resolvedObjectsNumberCache.put(taskId, resolvedNumber);
        }
        String failSqlFilePath = task.getTargetInstallPath() + String.format(
            TranscribeReplayConstants.FAIL_SQL_FILE_NAME,
            resolvedNumber / TranscribeReplayConstants.FAIL_FILE_SIZE + 1);
        ShellInfoVo shellInfo = transcribeReplayService.getShellInfo(taskId, TranscribeReplayConstants.TARGET_DB);
        boolean isFileExists = FileUtils.isRemoteFileExists(failSqlFilePath, shellInfo);
        if (!isFileExists) {
            return;
        }
        String failSqlFileContents = FileUtils.catRemoteFileContents(failSqlFilePath, shellInfo);
        if (failSqlFileContents.isEmpty()) {
            return;
        }
        int startObjectIndex = resolvedNumber % 100;
        String[] parts = failSqlFileContents.split(TaskAlertConstants.OBJECT_SEPARATOR);
        for (int i = startObjectIndex; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                FailSqlModel failSqlModel = JSON.parseObject(part, FailSqlModel.class);
                failSqlModel.setId(null);
                failSqlModel.setTaskId(taskId);
                save(failSqlModel);
                for (ParamModel paramModel : failSqlModel.getParameters()) {
                    paramModel.setFailSqlId(failSqlModel.getId());
                    transcribeReplayParamMapper.insert(paramModel);
                }
                resolvedNumber++;
            }
        }
        resolvedObjectsNumberCache.put(taskId, resolvedNumber);
    }

    /**
     * countFailSqlByTaskId
     *
     * @param taskId taskId
     * @return int
     */
    public int countFailSqlByTaskId(int taskId) {
        LambdaQueryWrapper<FailSqlModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FailSqlModel::getTaskId, taskId);
        return (int) count(queryWrapper);
    }
}
