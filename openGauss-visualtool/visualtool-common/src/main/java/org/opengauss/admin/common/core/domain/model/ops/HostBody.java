package org.opengauss.admin.common.core.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.utils.ops.JschUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;

/**
 * @author lhf
 * @date 2022/8/11 20:07
 **/
@Slf4j
@Data
public class HostBody {
    @NotEmpty(message = "The IP address cannot be empty")
    private String publicIp;
    @NotEmpty(message = "The Intranet IP address cannot be empty")
    private String privateIp;
    private String password;
    @NotEmpty(message = "Please select AZ")
    private String azId;
    private String remark;
    private Integer port;

    public OpsHostEntity toHostEntity(JschUtil jschUtil, String hostName) {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setPublicIp(publicIp);
        hostEntity.setPrivateIp(privateIp);
        hostEntity.setPort(port);
        hostEntity.setAzId(azId);
        hostEntity.setHostname(hostName);
        return hostEntity;
    }

    public OpsHostUserEntity toRootUser(String hostId) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("root");
        hostUserEntity.setHostId(hostId);
        return hostUserEntity;
    }
}
