package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

/**
 * for concurrent in future
 */
@Data
public class DadInstance {
    private String ip;
    private String port;
    private DadInstanceStatus status;
    public static enum DadInstanceStatus {
        RUNNING,
        STOPPED
    }
}
