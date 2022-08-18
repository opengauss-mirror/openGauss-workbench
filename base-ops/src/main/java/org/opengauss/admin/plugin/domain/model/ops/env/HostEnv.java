package org.opengauss.admin.plugin.domain.model.ops.env;

import lombok.Data;

/**
 * Host environment monitoring
 *
 * @author lhf
 * @date 2022/8/10 10:31
 **/
@Data
public class HostEnv {
    /**
     * hardware environment
     */
    private HardwareEnv hardwareEnv;
    /**
     * Software Environment
     */
    private SoftwareEnv softwareEnv;
}
