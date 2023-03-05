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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public TableDataInfo page(@RequestParam(required = false, value = "name") String name) {
        Page<JdbcDbClusterVO> page = opsJdbcDbClusterService.page(name, startPage());
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
}
