package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/8/5 09:05
 **/
@Data
public class InstallBody {
    private InstallContext installContext;

    private Boolean quickInstall;

    private String quickInstallResourceUrl;

    private String businessId;
}
