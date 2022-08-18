package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.plugin.enums.ops.HostAuthenticationTypeEnum;
import lombok.Data;

/**
 * Host credential
 *
 * @author lhf
 * @date 2022/8/4 22:17
 **/
@Data
public class HostCredentials {

    private String principal;

    private String credentials;

    private HostAuthenticationTypeEnum authenticationType;
}
