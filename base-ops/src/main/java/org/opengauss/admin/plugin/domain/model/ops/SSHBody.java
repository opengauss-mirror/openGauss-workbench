package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/8/8 13:57
 **/
@Data
public class SSHBody {
    private String hostId;

    private String rootPassword;

    private String businessId;
}
