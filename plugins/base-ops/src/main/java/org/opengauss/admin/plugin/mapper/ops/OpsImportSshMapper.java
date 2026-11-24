/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
