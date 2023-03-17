package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class OpenLooKengDto {
    private ServerDto serverDto;
    private String installPath;
    private String sourcePath;
    private int port;
    private OlkParamDto olkConfigDto;
}
