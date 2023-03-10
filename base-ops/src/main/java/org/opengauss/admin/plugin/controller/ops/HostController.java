package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
