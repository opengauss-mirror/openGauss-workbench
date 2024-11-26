/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * OpsImportSshMapper.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/mapper/ops/OpsImportSshMapper.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.mapper.ops;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.entity.ops.OpsImportSshEntity;

import java.util.List;

/**
 * OpsImportSshMapper
 * <p>
 * &#064;author:  wangchao
 * &#064;Date:  2024/11/4 12:02
 * &#064;Description:  OpsImportSshMapper
 *
 * @since 7.0.0
 **/
@Mapper
public interface OpsImportSshMapper {
    /**
     * query port, password HostIdAndInstallUserId of installUserName by device publicIp
     *
     * @param publicIp,installUserName publicIp,installUserName
     * @return List<OpsImportSshEntity>
     */
    OpsImportSshEntity queryHostInfo(@Param("installUserName") String installUserName,
        @Param("publicIp") String publicIp);

    /**
     * query clusterId of cluster by publicIp and databasePort
     *
     * @param publicIp,databasePort publicIp,databasePort
     * @return List<String>
     */
    List<String> checkPublicIpAndPort(@Param("publicIp") String publicIp, @Param("databasePort") String databasePort);
}
