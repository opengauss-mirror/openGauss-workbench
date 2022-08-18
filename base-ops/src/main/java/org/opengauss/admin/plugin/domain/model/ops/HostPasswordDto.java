package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/11/28 18:21
 **/
@Data
public class HostPasswordDto {
    private String hostId;
    private String rootPassword;
}
