package org.opengauss.admin.plugin.vo;

import lombok.Data;

import java.util.List;

/**
 * @className: TargetClusterVO
 * @author: xielibo
 * @date: 2023-03-23 12:24
 **/
@Data
public class TargetClusterVO {

    private String clusterId;
    private String clusterName;
    private String version;
    private String versionNum;
    private List<TargetClusterNodeVO> clusterNodes;
}
