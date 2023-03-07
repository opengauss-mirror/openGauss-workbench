package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.utils.ops.JschUtil;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;

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
    private Boolean isRemember;
    @NotEmpty(message = "Please select AZ")
    private String azId;
    private String remark;
    private Integer port;

    public OpsHostEntity toHostEntity(String hostName,String os,String cpuArch) {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setPublicIp(publicIp);
        hostEntity.setPrivateIp(privateIp);
        hostEntity.setPort(port);
        hostEntity.setAzId(azId);
        hostEntity.setHostname(hostName);
        hostEntity.setRemark(remark);
        hostEntity.setOs(os);
        hostEntity.setCpuArch(cpuArch);
        return hostEntity;
    }

    public OpsHostUserEntity toRootUser(String hostId) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("root");
        hostUserEntity.setHostId(hostId);
        if (Objects.nonNull(isRemember) && isRemember){
            hostUserEntity.setPassword(password);
        }
        return hostUserEntity;
    }
}
