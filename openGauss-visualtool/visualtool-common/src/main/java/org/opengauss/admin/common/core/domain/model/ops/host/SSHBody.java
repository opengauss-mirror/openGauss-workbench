package org.opengauss.admin.common.core.domain.model.ops.host;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/8/8 13:57
 **/
@Data
public class SSHBody {
    private String ip;

    private Integer sshPort;

    private String sshUsername;

    private String sshPassword;

    private String businessId;
}
