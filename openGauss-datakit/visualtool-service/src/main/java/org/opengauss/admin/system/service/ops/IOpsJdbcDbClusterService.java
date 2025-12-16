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
 * IOpsJdbcDbClusterService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IOpsJdbcDbClusterService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterImportAnalysisVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author lhf
 * @date 2023/1/13 11:07
 **/
public interface IOpsJdbcDbClusterService extends IService<OpsJdbcDbClusterEntity> {
    void add(JdbcDbClusterInputDto clusterInput);

    /**
     * Delete jdbc cluster by cluster id
     *
     * @param clusterId cluster id
     */
    void delete(String clusterId);

    /**
     * batch delete jdbc cluster
     *
     * @param ids cluster ids
     */
    void batchDelete(List<Long> ids);

    void update(String clusterId, JdbcDbClusterInputDto clusterInput);

    JdbcDbClusterImportAnalysisVO importAnalysis(MultipartFile file);

    void importCluster(MultipartFile file);

    List<JdbcDbClusterVO> listByType(String type);

    /**
     * List jdbc cluster vo by type
     *
     * @param dbType database type
     * @return jdbc cluster info
     */
    List<JdbcDbClusterVO> listByType(DbTypeEnum dbType);

    /**
     * List jdbc cluster entity by name
     *
     * @param name cluster name
     * @return jdbc cluster info
     */
    List<OpsJdbcDbClusterEntity> listByName(String name);

    /**
     * Get jdbc cluster vo by cluster id
     *
     * @param clusterId cluster id
     * @return jdbc cluster vo
     */
    JdbcDbClusterVO getJdbcClusterVoByClusterId(String clusterId);

    /**
     * List jdbc cluster entity by name and type
     *
     * @param name cluster name
     * @param type database type
     * @return jdbc cluster info
     */
    List<OpsJdbcDbClusterEntity> listByNameAndType(String name, String type);

    /**
     * List jdbc cluster vo by name and ip
     *
     * @param name cluster name
     * @param ip ip
     * @return jdbc cluster info
     */
    List<JdbcDbClusterVO> getJdbcClusterVosByNameAndIp(String name, String ip);

    /**
     * List jdbc cluster vo by name, ip and type
     *
     * @param name cluster name
     * @param ip ip
     * @param type database type
     * @return jdbc cluster info
     */
    List<JdbcDbClusterVO> getJdbcClusterVosByNameIpAndType(String name, String ip, String type);

    /**
     * Get jdbc cluster version by cluster id and db type
     *
     * @param clusterId cluster id
     * @param dbType database type
     * @return jdbc cluster version
     */
    String version(String clusterId, DbTypeEnum dbType);
}
