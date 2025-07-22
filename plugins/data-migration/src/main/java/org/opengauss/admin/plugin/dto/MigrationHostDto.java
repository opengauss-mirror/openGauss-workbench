/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.vo.HostBaseInfoVo;

import java.util.List;

/**
 * Migration host dto
 *
 * @since 2023/5/29
 */
@Data
public class MigrationHostDto {
    private List<MigrationTask> tasks;
    private HostBaseInfoVo baseInfos;
    private MigrationHostPortalInstall installInfo;
    private OpsHostEntity hostInfo;
    private Integer installPortalStatus;
}
