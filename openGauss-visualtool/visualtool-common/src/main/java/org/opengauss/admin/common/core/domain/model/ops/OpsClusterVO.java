package org.opengauss.admin.common.core.domain.model.ops;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/9/26 17:34
 **/
@Data
public class OpsClusterVO {

    private String clusterId;
    private String clusterName;
    private String version;
    private String versionNum;
    private String databasePassword;
    private DeployTypeEnum deployType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCheckAt;

    private Map<String, Integer> checkSummary;

    private List<OpsClusterNodeVO> clusterNodes;

    public static OpsClusterVO of(OpsClusterEntity opsClusterEntity) {
        OpsClusterVO opsClusterVO = new OpsClusterVO();
        opsClusterVO.setClusterId(opsClusterEntity.getClusterId());
        opsClusterVO.setClusterName(opsClusterEntity.getClusterName());
        opsClusterVO.setVersion(opsClusterEntity.getVersion().name());
        opsClusterVO.setVersionNum(opsClusterEntity.getVersionNum());
        opsClusterVO.setDatabasePassword(opsClusterEntity.getDatabasePassword());
        opsClusterVO.setDeployType(opsClusterEntity.getDeployType());
        return opsClusterVO;
    }
}
