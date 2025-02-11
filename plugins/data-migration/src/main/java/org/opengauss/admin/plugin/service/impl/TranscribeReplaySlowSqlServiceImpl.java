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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.util.StrUtil;

import org.opengauss.admin.plugin.domain.TranscribeReplaySlowSql;
import org.opengauss.admin.plugin.mapper.TranscribeReplaySlowSqlMapper;
import org.opengauss.admin.plugin.service.TranscribeReplaySlowSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * TranscribeReplaySlowSqlServiceImpl
 *
 * @since 2025-02-10
 */

@Service
public class TranscribeReplaySlowSqlServiceImpl
    extends ServiceImpl<TranscribeReplaySlowSqlMapper, TranscribeReplaySlowSql>
    implements TranscribeReplaySlowSqlService {
    @Autowired
    private TranscribeReplaySlowSqlMapper transcribeReplaySlowSqlMapper;

    @Override
    public IPage<TranscribeReplaySlowSql> selectList(Page startPage, Integer id, String sql) {
        LambdaQueryWrapper<TranscribeReplaySlowSql> lambdaQuery = Wrappers.lambdaQuery(TranscribeReplaySlowSql.class);
        lambdaQuery.eq(Objects.nonNull(id), TranscribeReplaySlowSql::getTaskId, id)
            .like(StrUtil.isNotEmpty(sql), TranscribeReplaySlowSql::getSqlStr, sql);
        return page(startPage, lambdaQuery);
    }

    @Override
    public List<TranscribeReplaySlowSql> getListByTaskId(Integer taskId) {
        LambdaQueryWrapper<TranscribeReplaySlowSql> lambdaQuery = Wrappers.lambdaQuery(TranscribeReplaySlowSql.class);
        lambdaQuery.eq(Objects.nonNull(taskId), TranscribeReplaySlowSql::getTaskId, taskId);
        return transcribeReplaySlowSqlMapper.selectList(lambdaQuery);
    }
}
