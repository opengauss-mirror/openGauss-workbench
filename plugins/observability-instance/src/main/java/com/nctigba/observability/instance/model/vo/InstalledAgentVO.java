/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  InstalledAgentVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/vo/InstalledAgentVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.vo;

import lombok.Data;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.ArrayList;
import java.util.List;

/**
 * InstalledAgentVO
 *
 * @since 2023/12/1
 */
@Data
public class InstalledAgentVO {
    private String envId;
    private String hostId;
    private String username;
    private String exporterPort;
    private String hostName;
    private String hostPublicIp;
    private String path;

    private List<OpsClusterVO> clusters = new ArrayList<>();
}
