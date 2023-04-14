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
 * JdbcDbClusterNodeVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/jdbc/JdbcDbClusterNodeVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;

/**
 * @author lhf
 * @date 2023/1/13 14:26
 **/
@Data
public class JdbcDbClusterNodeVO {
    private String clusterNodeId;
    private String name;
    private String ip;
    private String port;
    private String username;
    private String password;
    private String url;
    private String os;

    public static JdbcDbClusterNodeVO of(OpsJdbcDbClusterNodeEntity clusterNodeEntity, String os) {
        JdbcDbClusterNodeVO jdbcDbClusterNodeVO = new JdbcDbClusterNodeVO();
        jdbcDbClusterNodeVO.setClusterNodeId(clusterNodeEntity.getClusterNodeId());
        jdbcDbClusterNodeVO.setName(clusterNodeEntity.getName());
        jdbcDbClusterNodeVO.setIp(clusterNodeEntity.getIp());
        jdbcDbClusterNodeVO.setPort(clusterNodeEntity.getPort());
        jdbcDbClusterNodeVO.setUsername(clusterNodeEntity.getUsername());
        jdbcDbClusterNodeVO.setPassword(clusterNodeEntity.getPassword());
        jdbcDbClusterNodeVO.setUrl(clusterNodeEntity.getUrl());
        jdbcDbClusterNodeVO.setOs(os);
        return jdbcDbClusterNodeVO;
    }
}
