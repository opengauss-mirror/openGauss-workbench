/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * JdbcDbClusterInputDto.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/jdbc/JdbcDbClusterInputDto.java
 *
 * -------------------------------------------------------------------------
 */


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
