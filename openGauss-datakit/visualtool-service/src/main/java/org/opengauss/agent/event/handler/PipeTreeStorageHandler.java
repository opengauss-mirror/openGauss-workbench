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

package org.opengauss.agent.event.handler;

import com.lmax.disruptor.EventHandler;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.enums.agent.StoragePolicy;
import org.opengauss.agent.event.PipelineEvent;

import java.util.Objects;

/**
 * PipeTreeStorageHandler
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:21
 * @Description: PipeTreeStorageHandler
 * @since 7.0.0-RC2
 **/
@Slf4j
public class PipeTreeStorageHandler implements EventHandler<PipelineEvent> {
    public PipeTreeStorageHandler() {
    }

    @Override
    public void onEvent(PipelineEvent event, long sequence, boolean isEndOfBatch) {
        if (Objects.equals(StoragePolicy.TREE, event.getStoragePolicy())) {
            log.info("tree metric onEvent with {} event {} ", sequence, isEndOfBatch);
        }
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onShutdown() {
    }
}
