package org.opengauss.admin.system.service.ops;

import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;

/**
 * Cluster Installation Service Provider Specification
 *
 * @author lhf
 * @date 2022/8/12 09:09
 **/
public interface ClusterOpsProvider {
    /**
     * Installed openGauss version
     *
     * @return openGauss version
     */
    OpenGaussVersionEnum version();
}
