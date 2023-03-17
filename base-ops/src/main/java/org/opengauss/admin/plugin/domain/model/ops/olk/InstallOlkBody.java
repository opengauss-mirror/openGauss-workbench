package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;

@Data
public class InstallOlkBody {
    private String businessId;
    private InstallOlkContext installContext;
}
