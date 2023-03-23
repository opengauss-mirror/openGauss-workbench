package org.opengauss.admin.plugin.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkBody;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkPageVO;
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
        opsOlkService.removeById(id);
        return AjaxResult.success();
    }

    @DeleteMapping("/destroy/{id}")
    public AjaxResult destroy(@PathVariable("id") String id, @RequestParam String bid) {
        try {
            opsOlkService.destroy(id, bid);
        } catch (OpsException ex) {
            return AjaxResult.error("Destroy OpenLooKeng Failed: " + ex.getMessage());
        }
        return AjaxResult.success();
    }

    @PostMapping("/generateRuleYaml")
    public AjaxResult generateRuleYaml(@RequestBody OlkConfig olkConfig) {
        String result = opsOlkService.generateRuleYaml(olkConfig);
        return AjaxResult.success("ok", result);
    }

    @GetMapping("/start/{id}")
    public AjaxResult start(@PathVariable("id") String id, @RequestParam String bid) {
        try {
            opsOlkService.start(id, bid);
        } catch (OpsException ex) {
            return AjaxResult.error("Start OpenLooKeng Failed: " + ex.getMessage());
        }
        return AjaxResult.success();
    }

    @GetMapping("/stop/{id}")
    public AjaxResult stop(@PathVariable("id") String id, @RequestParam String bid) {
        try {
            opsOlkService.stop(id, bid);
        } catch (OpsException ex) {
            return AjaxResult.error("Stop OpenLooKeng Failed: " + ex.getMessage());
        }
        return AjaxResult.success();
    }

    @GetMapping("/page")
    public TableDataInfo list(@RequestParam(required = false, value = "name") String name) {
        IPage<OlkPageVO> page = opsOlkService.pageOlk(startPage(), name);
        return getDataTable(page);
    }
}
