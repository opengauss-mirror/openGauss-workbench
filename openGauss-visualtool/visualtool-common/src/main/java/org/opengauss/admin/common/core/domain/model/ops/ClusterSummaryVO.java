package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/23 23:21
 **/
@Data
public class ClusterSummaryVO {
    private Long clusterNum;
    private Long hostNum;
    private Long nodeNum;
}
