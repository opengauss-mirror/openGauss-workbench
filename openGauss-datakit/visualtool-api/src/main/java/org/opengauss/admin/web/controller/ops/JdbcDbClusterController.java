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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterImportAnalysisVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author lhf
 * @date 2023/1/13 11:10
 **/
@RestController
@RequestMapping("/jdbcDbCluster")
public class JdbcDbClusterController extends BaseController {
    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;

    @PostMapping("/add")
    public AjaxResult add(@RequestBody JdbcDbClusterInputDto clusterInput) {
        opsJdbcDbClusterService.add(clusterInput);
        return AjaxResult.success();
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false, value = "name") String name,@RequestParam(required = false,value = "ip") String ip, @RequestParam(required = false,value = "type") String type) {
        Page<JdbcDbClusterVO> page = opsJdbcDbClusterService.page(name,ip,type,startPage());
        return getDataTable(page);
    }

    @DeleteMapping("/{clusterId}")
    public AjaxResult del(@PathVariable("clusterId") String clusterId) {
        opsJdbcDbClusterService.del(clusterId);
        return AjaxResult.success();
    }

    @PutMapping("/{clusterId}")
    public AjaxResult update(@PathVariable("clusterId") String clusterId, @RequestBody JdbcDbClusterInputDto clusterInput) {
        opsJdbcDbClusterService.update(clusterId, clusterInput);
        return AjaxResult.success();
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
