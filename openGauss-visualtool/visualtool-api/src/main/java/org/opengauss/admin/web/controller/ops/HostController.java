package org.opengauss.admin.web.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.system.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Host
 *
 * @author lhf
 * @date 2022/8/7 22:24
 **/
@RestController
@RequestMapping("/host")
public class HostController extends BaseController {

    @Autowired
    private IHostService hostService;

    @PostMapping
    public AjaxResult add(@RequestBody @Validated HostBody hostBody) {
        hostService.add(hostBody);
        return AjaxResult.success();
    }

    @GetMapping("/listAll")
    public AjaxResult listAll(@RequestParam(value = "azId", required = false) String azId) {
        List<OpsHostEntity> hostEntityList = hostService.listAll(azId);
        return AjaxResult.success(hostEntityList);
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) String name) {
        IPage<OpsHostVO> page = hostService.pageHost(startPage(), name);
        return getDataTable(page);
    }

    @PostMapping("/ping")
    public AjaxResult ping(@RequestBody @Validated HostBody hostBody) {
        return toAjax(hostService.ping(hostBody));
    }

    @GetMapping("/ping/{hostId}")
    public AjaxResult ping(@PathVariable String hostId, @RequestParam(value = "rootPassword",required = false) String rootPassword) {
        return toAjax(hostService.ping(hostId, rootPassword));
    }

    @DeleteMapping("/{hostId}")
    public AjaxResult del(@PathVariable String hostId) {
        return toAjax(hostService.del(hostId));
    }

    @PutMapping("/{hostId}")
    public AjaxResult edit(@PathVariable String hostId,
                           @RequestBody @Validated HostBody hostBody) {
        return toAjax(hostService.edit(hostId, hostBody));
    }

    @PostMapping("/ssh")
    public AjaxResult ssh(@RequestBody SSHBody sshBody) {
        hostService.ssh(sshBody);
        return AjaxResult.success();
    }

    @GetMapping("/monitor")
    public AjaxResult monitor(@RequestParam String hostId, @RequestParam String businessId, @RequestParam(value = "rootPassword",required = false) String rootPassword){
        return AjaxResult.success(hostService.monitor(hostId,businessId,rootPassword));
    }
}
