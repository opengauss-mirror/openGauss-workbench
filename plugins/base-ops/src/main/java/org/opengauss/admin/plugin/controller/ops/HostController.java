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
 * base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/HostController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lhf
 * @date 2023/2/14 13:58
 **/
@RestController
@RequestMapping("/host")
public class HostController extends BaseController {

    @Autowired
    private IHostService hostService;

    @GetMapping("/pathEmpty/{id}")
    public AjaxResult pathEmpty(@PathVariable("id") String id,@RequestParam("path") String path,@RequestParam(value = "rootPassword",required = false) String rootPassword){
        return AjaxResult.success(hostService.pathEmpty(id,path,rootPassword));
    }

    @GetMapping("/portUsed/{id}")
    public AjaxResult portUsed(@PathVariable("id") String id,@RequestParam("port") Integer port,@RequestParam(value = "rootPassword",required = false) String rootPassword){
        return AjaxResult.success(hostService.portUsed(id,port,rootPassword));
    }

    @GetMapping("/fileExist/{id}")
    public AjaxResult fileExist(@PathVariable("id") String id,@RequestParam("file") String file,@RequestParam(value = "rootPassword",required = false) String rootPassword){
        return AjaxResult.success(hostService.fileExist(id,file,rootPassword));
    }

    /**
     * query disk path
     *
     * @param id the host id
     * @param rootPassword root user password
     * @return list of disk path
     */
    @GetMapping("/multiPathQuery/{id}")
    public AjaxResult multiPathQuery(@PathVariable("id") String id, @RequestParam(value = "rootPassword", required =
            false) String rootPassword) {
        List<String> lunPathList = hostService.multiPathQuery(id, rootPassword);
        return AjaxResult.success(lunPathList);
    }
}
