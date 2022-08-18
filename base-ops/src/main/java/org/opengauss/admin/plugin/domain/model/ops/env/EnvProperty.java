package org.opengauss.admin.plugin.domain.model.ops.env;

import org.opengauss.admin.plugin.enums.ops.HostEnvStatusEnum;
import lombok.Data;

/**
 * Environment properties
 *
 * @author lhf
 * @date 2022/8/10 10:33
 **/
@Data
public class EnvProperty {
    /**
     * queue number
     */
    private Integer sortNum;
    /**
     * property name
     */
    private String name;
    /**
     * property value
     */
    private String value;
    /**
     * property status
     */
    private HostEnvStatusEnum status;
    /**
     * property status message
     */
    private String statusMessage;
}
