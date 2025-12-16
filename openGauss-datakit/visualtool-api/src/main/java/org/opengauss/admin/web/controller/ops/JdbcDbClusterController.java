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
 * JdbcDbClusterController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/JdbcDbClusterController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterImportAnalysisVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.dto.BatchDeleteRequest;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.system.service.DbClusterInstanceService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author lhf
 * @date 2023/1/13 11:10
 **/
@RestController
@RequestMapping("/jdbcDbCluster")
public class JdbcDbClusterController extends BaseController {
    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;

    @Autowired
    private DbClusterInstanceService dbClusterInstanceService;

    /**
     * Add cluster
     *
     * @param clusterInput cluster information
     * @return success
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody JdbcDbClusterInputDto clusterInput) {
        dbClusterInstanceService.add(clusterInput);
        return AjaxResult.success();
    }

    /**
     * Get cluster page
     *
     * @param name cluster name
     * @param ip cluster ip
     * @param type cluster type
     * @return cluster list
     */
    @GetMapping("/page")
    public TableDataInfo selectPage(
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "ip") String ip,
            @RequestParam(required = false, value = "type") String type
    ) {
        return dbClusterInstanceService.page(name, ip, type);
    }

    /**
     * Delete cluster
     *
     * @param clusterId cluster id
     * @return success
     */
    @DeleteMapping("/{clusterId}")
    public AjaxResult del(@PathVariable("clusterId") String clusterId) {
        dbClusterInstanceService.delete(clusterId);
        return AjaxResult.success();
    }

    /**
     * Delete clusters
     *
     * @param request batch delete request
     * @return success
     */
    @DeleteMapping("/batch")
    public AjaxResult delete(@RequestBody BatchDeleteRequest request) {
        dbClusterInstanceService.batchDelete(request.getIds());
        return AjaxResult.success();
    }

    /**
     * Update cluster information
     *
     * @param clusterId cluster id
     * @param clusterInput cluster information
     * @return success
     */
    @PutMapping("/{clusterId}")
    public AjaxResult update(
            @PathVariable("clusterId") String clusterId, @RequestBody JdbcDbClusterInputDto clusterInput
    ) {
        dbClusterInstanceService.update(clusterId, clusterInput);
        return AjaxResult.success();
    }

    /**
     * Get cluster version number
     *
     * @param clusterId cluster id
     * @param dbType database type
     * @return version number
     */
    @GetMapping("/version/{clusterId}")
    public AjaxResult version(@PathVariable("clusterId") String clusterId, DbTypeEnum dbType) {
        return AjaxResult.success(dbClusterInstanceService.version(clusterId, dbType));
    }

    @PostMapping("/importAnalysis")
    public AjaxResult importAnalysis(@RequestParam("file") MultipartFile file) {
        JdbcDbClusterImportAnalysisVO analysisVO = opsJdbcDbClusterService.importAnalysis(file);
        return AjaxResult.success(analysisVO);
    }

    @PostMapping("/importCluster")
    public AjaxResult importCluster(@RequestParam("file") MultipartFile file) {
        opsJdbcDbClusterService.importCluster(file);
        return AjaxResult.success();
    }

    @GetMapping("/downloadTemplate")
    public ResponseEntity<byte[]> downloadTemplate(){
        String fileName = "JDBC_IMPORT_TEMPLATE.csv";
        try {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {

        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition", "attachment;filename=" + fileName);

        String content = "\"集群名称（自定义一个集群名，集群名相同则认为是同一个集群）\",\"连接URL（JDBC的URL信息，例如 jdbc:opengauss://IP:PORT/databasename）\",\"用户名（数据库连接用户名）\",\"密码（数据库用户名对应的密码）\"\n" +
                "\"\",\"\",\"\",\"\"";
        return ResponseEntity.ok().headers(headers).body(content.getBytes(StandardCharsets.UTF_8));
    }
}
