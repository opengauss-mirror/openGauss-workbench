package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class OlkParamDto {
    private String xmx = "16G";
    private String maxMemory = "50GB";
    private String maxTotalMemory = "50GB";
    private String maxMemoryPer = "10GB";
    private String maxTotalMemoryPer = "10GB";
}
