package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/9 22:43
 **/
@Data
public class NodeNetMonitor {
    private String face;
    private String receive;
    private String transmit;
}
