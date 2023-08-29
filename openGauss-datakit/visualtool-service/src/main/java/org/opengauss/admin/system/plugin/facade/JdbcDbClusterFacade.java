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
 * JdbcDbClusterFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/JdbcDbClusterFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lhf
 * @date 2023/1/14 22:22
 **/
@Service
public class JdbcDbClusterFacade {
    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;

    public void add(JdbcDbClusterInputDto clusterInput){
        opsJdbcDbClusterService.add(clusterInput);
    }

    public Page<JdbcDbClusterVO> page(String name, Page page) {
        return opsJdbcDbClusterService.page(name, null, null, page);
    }

    public Page<JdbcDbClusterVO> page(String name,String ip,String type, Page page) {
        return opsJdbcDbClusterService.page(name, ip, type, page);
    }

    public List<JdbcDbClusterVO> listAll(String type) {
        return opsJdbcDbClusterService.listByType(type);
    }

    public List<JdbcDbClusterVO> listAll() {
        return opsJdbcDbClusterService.listAll();
    }
}
