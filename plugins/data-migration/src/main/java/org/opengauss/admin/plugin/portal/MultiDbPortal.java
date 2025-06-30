/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MULTI_DB portal
 *
 * @since 2025/06/23
 */
@Slf4j
@Component
public class MultiDbPortal extends MigrationPortal {
    /**
     * install portal
     *
     * @param hostId host id
     * @param install install info
     * @param isReinstall is retry install
     * @return AjaxResult.success
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult install(String hostId, MigrationHostPortalInstall install, boolean isReinstall) {
        return AjaxResult.success();
    }

    /**
     * delete portal
     *
     * @param hostId host id
     * @return AjaxResult.success
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deletePortal(String hostId) {
        return AjaxResult.success();
    }
}
