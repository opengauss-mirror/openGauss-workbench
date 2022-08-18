package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

import java.util.List;

/**
 * @author lhf
 * @date 2022/9/29 17:18
 **/
@Data
public class OpsClusterBody {
    private String clusterId;

    private List<String> nodeIds;

    private String businessId;

    private Boolean sync;
}
