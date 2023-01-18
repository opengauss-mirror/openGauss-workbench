package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.List;

/**
 * @author lhf
 * @date 2023/1/13 13:27
 **/
@Data
public class JdbcDbClusterInputDto {
    private String clusterName;
    private DeployTypeEnum deployType;
    private List<JdbcDbClusterNodeInputDto> nodes;
    private String remark;

    public static JdbcDbClusterInputDto of(String name, DeployTypeEnum deployType) {
        JdbcDbClusterInputDto inputDto = new JdbcDbClusterInputDto();
        inputDto.setClusterName(name);
        inputDto.setDeployType(deployType);
        return inputDto;
    }
}
