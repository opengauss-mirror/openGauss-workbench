/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * OpsHostVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/host/OpsHostVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.host;

import lombok.Data;

import org.opengauss.admin.common.enums.agent.AgentStatus;

import java.util.Set;

/**
 * @author lhf
 * @date 2022/10/23 08:22
 **/
@Data
public class OpsHostVO {
    private String hostId;
    private String hostname;
    private String privateIp;
    private String publicIp;
    private Integer port;
    private String azId;
    private String azName;
    private String remark;
    private Boolean isRemember;
    private String os;
    private String cpuArch;
    private String name;
    private String agentName;
    private AgentStatus agentStatus;
    private String agentInstallPath;
    private String agentInstallUsername;
    private Integer agentInstallPort;
    private String osVersion;
    private Set<String> tags;
}
