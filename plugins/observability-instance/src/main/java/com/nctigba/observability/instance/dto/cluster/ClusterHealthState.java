/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import lombok.Data;

import java.util.Map;

/**
 * ClusterHealthState
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
public class ClusterHealthState {
    private String clusterState;
    private Map<String, String> nodeState;
    private Map<String, String> nodeRole;
    private Map<String, String> nodeName;
    private Map<String, String> cmState;
}
