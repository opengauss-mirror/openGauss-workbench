/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SyncSituation
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncSituation {
    private String hostIp;
    private String sync;
    private String walSyncState;
    private String syncPriority;
    private String receivedDelay;
    private String writeDelay;
    private String replayDelay;
}
