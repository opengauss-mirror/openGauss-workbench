package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
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
    @NotEmpty(message = "The password cannot be empty")
    private String password;
    @NotEmpty(message = "Please select AZ")
    private String azId;
    private String remark;
    private Integer port;

    public OpsHostUserEntity toRootUser(String hostId) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("root");
        hostUserEntity.setPassword(password);
        hostUserEntity.setHostId(hostId);
        return hostUserEntity;
    }
}
