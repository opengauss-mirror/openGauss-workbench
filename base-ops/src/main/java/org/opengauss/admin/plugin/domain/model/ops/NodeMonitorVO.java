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
 * NodeMonitorVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/NodeMonitorVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/10/9 22:40
 **/
@Data
public class NodeMonitorVO {
    private String cpu;
    private String memory;
    private List<NodeNetMonitor> net;
    private String lock;
    private String session;
    private String connectNum;
    private String kernel;
    private String memorySize;
    private List<Map<String, String>> sessionMemoryTop10;
    private String state;
    private Long time;
}
