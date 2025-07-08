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

package org.opengauss.agent.data;

import org.opengauss.admin.common.core.domain.model.agent.AgentClusterVo;

/**
 * TaskClusterConfigLoader
 *
 * @author: wangchao
 * @Date: 2025/4/18 11:38
 * @Description: TaskClusterConfigLoader
 * @since 7.0.0-RC2
 **/
public interface TaskClusterConfigLoader {
    /**
     * load cluster config info
     *
     * @param clusterNodeId clusterNodeId
     * @return AgentClusterVo
     */
    AgentClusterVo load(String clusterNodeId);
}
