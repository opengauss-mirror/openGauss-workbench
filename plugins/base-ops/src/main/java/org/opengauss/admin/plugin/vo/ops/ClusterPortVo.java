/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.OpsHostPortUsedStatusEnum;

/**
 * ClusterPortVo
 *
 * @author wangchao
 * @since  2024/6/22 9:41
 **/
@Data
public class ClusterPortVo {
    private String clusterNodeId;
    private String databasePort;
    private String cmPort;
    private OpsHostPortUsedStatusEnum databasePortStatus;
    private OpsHostPortUsedStatusEnum cmPortStatus;

    public ClusterPortVo(String clusterNodeId, String databasePort, String cmPort) {
        this.clusterNodeId = clusterNodeId;
        this.databasePort = databasePort;
        this.cmPort = cmPort;
    }
}