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
 */

package org.opengauss.agent.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;

import org.opengauss.admin.common.core.domain.entity.agent.MetricHistorical;
import org.opengauss.admin.system.mapper.agent.MetricHistoricalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MetricHistoricalStorageService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:10
 * @Description: MetricHistoricalStorageService
 * @since 7.0.0-RC2
 **/
@Service
public class MetricHistoricalStorageService extends ServiceImpl<MetricHistoricalMapper, MetricHistorical>
    implements IService<MetricHistorical> {
    private static final int BATCH_SIZE = 1000;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    /**
     * Delete old history data
     *
     * @param taskIds taskIds
     * @param cutoffTime cutoffTime
     * @return the number of deleted rows
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteOldHistoryData(List<Long> taskIds, Instant cutoffTime) {
        if (CollUtil.isEmpty(taskIds) || cutoffTime == null) {
            return 0;
        }
        int totalDeleted = 0;
        boolean hasMore;
        do {
            int deleted = deleteByTaskIdAndCutoffTime(taskIds, cutoffTime);
            if (deleted > 0) {
                totalDeleted += deleted;
            }
            hasMore = deleted == BATCH_SIZE;
            if (hasMore) {
                ThreadUtil.safeSleep(100);
            }
        } while (hasMore);
        return totalDeleted;
    }

    private int deleteByTaskIdAndCutoffTime(List<Long> taskIds, Instant cutoffTime) {
        ZonedDateTime zonedCutoffTime = cutoffTime.atZone(ZoneId.of("GMT+8"))
            .withZoneSameInstant(ZoneId.of("Asia/Shanghai")); // 或 ZoneId.of("UTC+8")
        return baseMapper.delete(Wrappers.lambdaQuery(MetricHistorical.class)
            .in(MetricHistorical::getTaskId, taskIds)
            .lt(MetricHistorical::getCreateTime, zonedCutoffTime.format(formatter))
            .last("limit " + BATCH_SIZE));
    }
}
