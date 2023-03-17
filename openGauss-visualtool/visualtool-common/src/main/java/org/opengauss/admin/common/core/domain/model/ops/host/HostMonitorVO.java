package org.opengauss.admin.common.core.domain.model.ops.host;

import lombok.Data;

/**
 * @author lhf
 * @date 2023/3/14 10:31
 **/
@Data
public class HostMonitorVO {
    private String upSpeed;
    private String downSpeed;
    private String cpu;
    private String memory;
    private String disk;
}
