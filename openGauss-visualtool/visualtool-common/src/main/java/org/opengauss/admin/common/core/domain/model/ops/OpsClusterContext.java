package org.opengauss.admin.common.core.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import lombok.Data;

import java.util.List;

/**
 * @author lhf
 * @date 2022/9/29 17:31
 **/
@Data
public class OpsClusterContext implements Cloneable {
    private WsSession retSession;

    private List<String> opNodeIds;

    private List<HostInfoHolder> hostInfoHolders;

    private OpsClusterEntity opsClusterEntity;

    private List<OpsClusterNodeEntity> opsClusterNodeEntityList;
}
