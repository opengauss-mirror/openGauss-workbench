package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkBody;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.ShardingDatasourceConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ShardingRuleDto;
import org.opengauss.admin.plugin.service.ops.IOpsOlkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/olk")
public class OpenLooKengController extends BaseController {
    @Autowired
    private IOpsOlkService opsOlkService;

    @PostMapping("/install")
    public AjaxResult install(@RequestBody InstallOlkBody installBody) {
        opsOlkService.install(installBody);
        return AjaxResult.success();
    }

    @DeleteMapping("/remove/{id}")
    public AjaxResult delete(@PathVariable("id") String id) {
        opsOlkService.remove(id);
        return AjaxResult.success();
    }

    @DeleteMapping("/destroy/{id}")
    public AjaxResult destroy(@PathVariable("id") String id) {
        opsOlkService.destroy(id);
        return AjaxResult.success();
    }

    @PostMapping("/generateRuleYaml")
    public AjaxResult generateRuleYaml(@RequestBody OlkConfig olkConfig) {
        String result = opsOlkService.generateRuleYaml(olkConfig);
        return AjaxResult.success("ok", result);
    }

    @PostMapping("/start/{id}")
    public AjaxResult start(@PathVariable("id") String id) {
        opsOlkService.start(id);
        return AjaxResult.success();
    }

    @PostMapping("/stop/{id}")
    public AjaxResult stop(@PathVariable("id") String id) {
        opsOlkService.stop(id);
        return AjaxResult.success();
    }
}
