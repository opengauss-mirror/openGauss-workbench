/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.service.task.group;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.enums.StoragePolicy;

/**
 * GroupKey
 *
 * @author: wangchao
 * @Date: 2025/5/16 16:40
 * @since 7.0.0-RC2
 **/
@Getter
@EqualsAndHashCode
public class GroupKey {
    private final String dataSendTarget;
    private final long period;
    private final StoragePolicy storagePolicy;

    /**
     * Constructor
     *
     * @param task task definition
     */
    public GroupKey(TaskDefinition task) {
        this.dataSendTarget = task.getDataSendTarget();
        this.period = task.getPeriod();
        this.storagePolicy = task.getStoragePolicy();
    }

    @Override
    public String toString() {
        return "groupKey=[" + dataSendTarget + "," + period + "," + storagePolicy + "]";
    }
}
