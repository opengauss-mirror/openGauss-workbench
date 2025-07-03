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

package org.opengauss.agent.entity.task;

import lombok.Data;
import lombok.ToString;

/**
 * AgentClusterVo
 *
 * @author: wangchao
 * @date: 2025/4/18 11:40
 * @since 7.0.0-RC2
 **/
@Data
public class AgentClusterVo {
    private String clusterId;
    private String clusterNodeId;
    private String dataBaseType;
    private String hostIp;
    private String port;
    private String username;
    @ToString.Exclude
    private String dbPassword;
    private String url;
}
