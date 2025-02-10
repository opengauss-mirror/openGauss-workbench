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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.TranscribeReplayHost;
import org.opengauss.admin.plugin.mapper.TranscribeReplayHostMapper;
import org.opengauss.admin.plugin.service.TranscribeReplayHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TranscribeReplayHostServiceImpl
 *
 * @since 2025-02-10
 */

@Service
public class TranscribeReplayHostServiceImpl extends ServiceImpl<TranscribeReplayHostMapper, TranscribeReplayHost>
    implements TranscribeReplayHostService {
    @Autowired
    private TranscribeReplayHostMapper transcribeReplayHostMapper;

    @Override
    public List<TranscribeReplayHost> getHostsByTaskId(Integer taskId) {
        LambdaQueryWrapper<TranscribeReplayHost> query = new LambdaQueryWrapper<>();
        query.eq(TranscribeReplayHost::getTaskId, taskId);
        return transcribeReplayHostMapper.selectList(query);
    }

    @Override
    public TranscribeReplayHost getHostByTaskIdAndDbType(Integer taskId, String dbType) {
        LambdaQueryWrapper<TranscribeReplayHost> query = new LambdaQueryWrapper<>();
        query.eq(TranscribeReplayHost::getTaskId, taskId).eq(TranscribeReplayHost::getDbType, dbType).last("limit 1");
        return getOne(query);
    }

    @Override
    public TranscribeReplayHost getReplayHostByTaskId(Integer taskId) {
        LambdaQueryWrapper<TranscribeReplayHost> query = new LambdaQueryWrapper<>();
        query.eq(TranscribeReplayHost::getTaskId, taskId)
            .eq(TranscribeReplayHost::getDbType, "openGauss")
            .last("limit 1");
        return getOne(query);
    }

    @Override
    public void deleteByTaskId(Integer taskId) {
        LambdaQueryWrapper<TranscribeReplayHost> query = new LambdaQueryWrapper<>();
        query.eq(TranscribeReplayHost::getTaskId, taskId);
        List<Integer> ids = list(query).stream().map(TranscribeReplayHost::getId).collect(Collectors.toList());
        removeBatchByIds(ids);
    }
}
