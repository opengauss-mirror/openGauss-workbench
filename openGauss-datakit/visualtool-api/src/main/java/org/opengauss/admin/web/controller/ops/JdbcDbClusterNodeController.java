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
 * JdbcDbClusterNodeController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/JdbcDbClusterNodeController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lhf
 * @date 2023/1/13 15:34
 **/
@RestController
@RequestMapping("/jdbcDbClusterNode")
public class JdbcDbClusterNodeController extends BaseController {

    @Autowired
    private IOpsJdbcDbClusterNodeService opsJdbcDbClusterNodeService;

    @DeleteMapping("/{clusterNodeId}")
    public AjaxResult del(@PathVariable("clusterNodeId") String clusterNodeId) {
        opsJdbcDbClusterNodeService.del(clusterNodeId);
        return AjaxResult.success();
    }

    @PutMapping("/{clusterNodeId}")
    public AjaxResult update(@PathVariable("clusterNodeId") String clusterNodeId, @RequestBody JdbcDbClusterNodeInputDto clusterNodeInput) {
        opsJdbcDbClusterNodeService.update(clusterNodeId, clusterNodeInput);
        return AjaxResult.success();
    }

    @PostMapping("/add/{clusterId}")
    public AjaxResult add(@PathVariable("clusterId") String clusterId, @RequestBody JdbcDbClusterNodeInputDto clusterNodeInput) {
        opsJdbcDbClusterNodeService.add(clusterId, clusterNodeInput);
        return AjaxResult.success();
    }

    @PostMapping("/ping")
    public AjaxResult ping(@RequestBody JdbcDbClusterNodeInputDto clusterNodeInput) {
        return AjaxResult.success(opsJdbcDbClusterNodeService.ping(clusterNodeInput));
    }

    @GetMapping("/monitor/{clusterNodeId}")
    public AjaxResult monitor(@PathVariable("clusterNodeId") String clusterNodeId,@RequestParam("businessId") String businessId){
        return AjaxResult.success(opsJdbcDbClusterNodeService.monitor(clusterNodeId,businessId));
    }

}
