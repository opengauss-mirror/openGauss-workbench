/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service.provider;

import java.util.List;

import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;

import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrScopeEnum;
import com.nctigba.observability.instance.service.provider.ClusterOpsProviderManager.OpenGaussSupportOSEnum;

/**
 * Cluster Installation Service Provider Specification
 *
 * @author lhf
 * @date 2022/8/12 09:09
 **/
public interface ClusterOpsProvider {
    OpenGaussVersionEnum version();

    OpenGaussSupportOSEnum os();

    void enableWdrSnapshot(Session session, OpsClusterEntity clusterEntity,
            List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath);
}
