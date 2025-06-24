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
 * JdbcDbClusterVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/jdbc/JdbcDbClusterVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.Date;
import java.util.List;

/**
 * @author lhf
 * @date 2023/1/13 14:26
 **/
@Data
public class JdbcDbClusterVO {
    private String clusterId;
    private String name;
    private DeployTypeEnum deployType;
    private DbTypeEnum dbType;
    private List<JdbcDbClusterNodeVO> nodes;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String versionNum;

    public static JdbcDbClusterVO of(OpsJdbcDbClusterEntity record, List<JdbcDbClusterNodeVO> nodes) {
        JdbcDbClusterVO jdbcDbClusterVO = new JdbcDbClusterVO();
        jdbcDbClusterVO.setClusterId(record.getClusterId());
        jdbcDbClusterVO.setName(record.getName());
        jdbcDbClusterVO.setDeployType(record.getDeployType());
        jdbcDbClusterVO.setDbType(record.getDbType());
        jdbcDbClusterVO.setUpdateTime(record.getUpdateTime());
        jdbcDbClusterVO.setNodes(nodes);
        return jdbcDbClusterVO;
    }
}
