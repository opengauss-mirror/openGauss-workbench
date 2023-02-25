package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

import java.util.Map;

/**
 * @author lhf
 * @date 2023/2/20 17:22
 **/
@Data
public class OmStatusModel {
    private boolean installCm;
    private Map<String,String> nodeIdMapHostname;
    private Map<String,String> hostnameMapNodeId;
}
