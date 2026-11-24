/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.domain.model.modeling;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import lombok.Data;

/**
 *
 * @author LZW
 */
@Data
public class OpenGaussConnectorBody {

    private String id;

    private String ip;

    private Integer port;

    private String database;

    private String dbUser;

    private String dbPassword;

    private String schema;

    private static final long serialVersionUID = 1L;

    public OpenGaussConnectorBody(OpsClusterNodeVO clusterNodeVO) {
        this.setIp(clusterNodeVO.getPublicIp());
        this.setPort(clusterNodeVO.getDbPort());
        this.setDatabase(clusterNodeVO.getDbName());
        this.setDbUser(clusterNodeVO.getDbUser());
        this.setDbPassword(clusterNodeVO.getDbUserPassword());

    }
}
