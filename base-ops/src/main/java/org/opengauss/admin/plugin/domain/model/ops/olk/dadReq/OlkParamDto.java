package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class OlkParamDto {
    private String xmx;
    private String maxMemory;
    private String maxTotalMemory;
    private String maxMemoryPer;
    private String maxTotalMemoryPer;
}
