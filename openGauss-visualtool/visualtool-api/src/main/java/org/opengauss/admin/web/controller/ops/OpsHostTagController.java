package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.system.service.ops.IOpsHostTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lhf
 * @date 2023/3/14 23:42
 **/
@RestController
@RequestMapping("/hostTag")
public class OpsHostTagController {
    @Autowired
    private IOpsHostTagService opsHostTagService;

    @PutMapping("/addTag")
    public AjaxResult addTag(@RequestBody HostTagInputDto hostTagInputDto){
        opsHostTagService.addTag(hostTagInputDto);
        return AjaxResult.success();
    }

    @GetMapping("/listAll")
    public AjaxResult listAll(){
        List<OpsHostTagEntity> list = opsHostTagService.list();
        return AjaxResult.success(list);
    }
}
