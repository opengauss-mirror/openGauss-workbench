package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.entity.ops.OpsWdrEntity;
import org.opengauss.admin.plugin.domain.model.ops.WdrGeneratorBody;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import org.opengauss.admin.plugin.service.ops.IOpsWdrService;
import org.opengauss.admin.plugin.vo.ops.DwrSnapshotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author lhf
 * @date 2022/10/13 15:14
 **/
@RestController
@RequestMapping("/wdr")
public class WdrController extends BaseController {

    @Autowired
    private IOpsWdrService wdrService;

    @GetMapping("/listSnapshot")
    public AjaxResult listSnapshot(@RequestParam String clusterId, @RequestParam String hostId) {
        List<DwrSnapshotVO> dwrSnapshotVOS = wdrService.listSnapshot(startPage(), clusterId, hostId);
        return AjaxResult.success(dwrSnapshotVOS);
    }

    @GetMapping("/createSnapshot")
    public AjaxResult createSnapshot(@RequestParam String clusterId, @RequestParam String hostId) {
        wdrService.createSnapshot(clusterId, hostId);
        return AjaxResult.success();
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam String clusterId,
                           @RequestParam WdrScopeEnum wdrScope,
                           @RequestParam WdrTypeEnum wdrType,
                           @RequestParam(required = false) String hostId,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end) {
        List<OpsWdrEntity> wdrList = wdrService.listWdr(clusterId, wdrScope, wdrType, hostId, start, end);
        return AjaxResult.success(wdrList);
    }

    @DeleteMapping("/del/{id}")
    public AjaxResult del(@PathVariable("id") String id) {
        wdrService.del(id);
        return AjaxResult.success();
    }

    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody @Validated WdrGeneratorBody wdrGeneratorBody) {
        wdrService.generate(wdrGeneratorBody);
        return AjaxResult.success();
    }

    @GetMapping("/downloadWdr")
    public void downloadWdr(@RequestParam String wdrId, HttpServletResponse response) {
        wdrService.downloadWdr(wdrId,response);
    }
}
