package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import lombok.Data;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;

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

    private OpenGaussSupportOSEnum os;

    private List<OpsClusterNodeEntity> opsClusterNodeEntityList;
}
