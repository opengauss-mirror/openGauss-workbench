package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

import java.util.List;

/**
 * Host Information
 *
 * @author lhf
 * @date 2022/8/4 22:12
 **/
@Data
public class Host {
    private String id;
    /**
     * The host address
     */
    private String host;
    /**
     * Host SSH Port
     */
    private Integer port;
    /**
     * Host authentication Certificate
     */
    private List<HostCredentials> hostCredentials;
}
