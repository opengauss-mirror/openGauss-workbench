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
 * HostController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/HostController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import java.util.Set;

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
    public TableDataInfo page(@RequestParam(required = false) String name, @RequestParam(value = "tagIds",required = false) Set<String> tagIds, @RequestParam(value = "os",required = false) String os) {
        IPage<OpsHostVO> page = hostService.pageHost(startPage(), name, tagIds, os);
        return getDataTable(page);
    }

    @PostMapping("/ping")
    public AjaxResult ping(@RequestBody @Validated HostBody hostBody) {
        boolean ping = hostService.ping(hostBody);
        return ping ? AjaxResult.success(): AjaxResult.error("Connectivity test failed");
    }

    @GetMapping("/ping/{hostId}")
    public AjaxResult ping(@PathVariable String hostId, @RequestParam(value = "rootPassword", required = false) String rootPassword) {
        boolean ping = hostService.ping(hostId, rootPassword);
        return ping ? AjaxResult.success(): AjaxResult.error("Connectivity test failed");
    }

    @DeleteMapping("/{hostId}")
    public AjaxResult del(@PathVariable String hostId) {
        boolean del = hostService.del(hostId);
        return del? AjaxResult.success() : AjaxResult.error("Failed to delete host");
    }

    @PutMapping("/{hostId}")
    public AjaxResult edit(@PathVariable String hostId,
                           @RequestBody @Validated HostBody hostBody) {
        boolean edit = hostService.edit(hostId, hostBody);
        return edit ? AjaxResult.success() : AjaxResult.error("Failed to edit host");
    }

    @PostMapping("/ssh")
    public AjaxResult ssh(@RequestBody SSHBody sshBody) {
        hostService.ssh(sshBody);
        return AjaxResult.success();
    }

    @PostMapping("/ssh/{hostId}")
    public AjaxResult ssh(@PathVariable("hostId") String hostId,@RequestBody SSHBody sshBody) {
        hostService.ssh(hostId,sshBody);
        return AjaxResult.success();
    }

    @GetMapping("/monitor")
    public AjaxResult monitor(@RequestParam String hostId, @RequestParam String businessId, @RequestParam(value = "rootPassword",required = false) String rootPassword){
        return AjaxResult.success(hostService.monitor(hostId,businessId,rootPassword));
    }
}
