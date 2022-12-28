package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;


/**
 * @author lhf
 * @date 2022/8/20 12:14
 **/
@Data
public class UnInstallBody {
    private String clusterId;

    private String businessId;

    private UnInstallContext unInstallContext;

    private Boolean force;
}
