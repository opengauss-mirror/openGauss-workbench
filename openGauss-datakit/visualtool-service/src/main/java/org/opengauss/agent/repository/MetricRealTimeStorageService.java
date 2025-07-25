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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.common.core.domain.entity.agent.MetricRealTime;
import org.opengauss.admin.system.mapper.agent.MetricRealTimeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MetricRealTimeStorageService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:10
 * @Description: MetricRealTimeStorageService
 * @since 7.0.0-RC2
 **/
@Service
public class MetricRealTimeStorageService extends ServiceImpl<MetricRealTimeMapper, MetricRealTime>
    implements IService<MetricRealTime> {
    /**
     * query real time by cluster node id
     *
     * @param clusterNodeId cluster node id
     * @return list
     */
    public List<MetricRealTime> queryRealTimeByClusterNodeId(String clusterNodeId) {
        LambdaQueryWrapper<MetricRealTime> wrapper = Wrappers.lambdaQuery(MetricRealTime.class);
        wrapper.eq(MetricRealTime::getClusterNodeId, clusterNodeId);
        return list(wrapper);
    }

    /**
     * query real time by agent id
     *
     * @param hostId agent id
     * @return list
     */
    public List<MetricRealTime> listAgentHostInfo(String hostId) {
        LambdaQueryWrapper<MetricRealTime> wrapper = Wrappers.lambdaQuery(MetricRealTime.class);
        wrapper.eq(MetricRealTime::getAgentId, hostId);
        return list(wrapper);
    }
}
