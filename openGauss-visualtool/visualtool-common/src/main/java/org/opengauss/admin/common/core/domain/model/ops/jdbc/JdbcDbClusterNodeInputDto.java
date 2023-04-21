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
 * JdbcDbClusterNodeInputDto.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/jdbc/JdbcDbClusterNodeInputDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

/**
 * @author lhf
 * @date 2023/1/13 13:31
 **/
@Data
public class JdbcDbClusterNodeInputDto {
    private String name;
    private String url;
    private String username;
    private String password;
    private String remark;
    private DeployTypeEnum deployType;

    public static JdbcDbClusterNodeInputDto of(String name, String url, String username, String password) {
        JdbcDbClusterNodeInputDto jdbcDbClusterNodeInputDto = new JdbcDbClusterNodeInputDto();
        jdbcDbClusterNodeInputDto.setName(name);
        jdbcDbClusterNodeInputDto.setUrl(url);
        jdbcDbClusterNodeInputDto.setUsername(username);
        jdbcDbClusterNodeInputDto.setPassword(password);
        return jdbcDbClusterNodeInputDto;
    }
}
