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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterImportAnalysisVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author lhf
 * @date 2023/1/13 11:07
 **/
public interface IOpsJdbcDbClusterService extends IService<OpsJdbcDbClusterEntity> {
    void add(JdbcDbClusterInputDto clusterInput);

    Page<JdbcDbClusterVO> page(String name, String ip, String type, Page page);

    void del(String clusterId);

    void update(String clusterId, JdbcDbClusterInputDto clusterInput);

    JdbcDbClusterImportAnalysisVO importAnalysis(MultipartFile file);

    void importCluster(MultipartFile file);

    List<JdbcDbClusterVO> listAll();

    List<JdbcDbClusterVO> listByType(String type);
}
