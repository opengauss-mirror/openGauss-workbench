package org.opengauss.admin.common.core.domain.model.ops.host;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/23 08:22
 **/
@Data
public class OpsHostVO {
    private String hostId;
    private String hostname;
    private String privateIp;
    private String publicIp;
    private Integer port;
    private String azId;
    private String azName;
    private String remark;
    private Boolean isRemember;
}
