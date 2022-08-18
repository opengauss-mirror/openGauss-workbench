package org.opengauss.admin.web.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.system.service.ops.IOpsAzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AZ
 *
 * @author lhf
 * @date 2022/8/6 20:45
 **/
@RestController
@RequestMapping("/az")
public class AzController extends BaseController {

    @Autowired
    private IOpsAzService opsAzService;

    @PostMapping
    public AjaxResult add(@Validated @RequestBody OpsAzEntity az) {
        return toAjax(opsAzService.add(az));
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false, value = "name") String name) {
        LambdaQueryWrapper<OpsAzEntity> queryWrapper = Wrappers.lambdaQuery(OpsAzEntity.class)
                .like(StrUtil.isNotEmpty(name), OpsAzEntity::getName, name);
        IPage<OpsAzEntity> page = opsAzService.page(startPage(), queryWrapper);
        return getDataTable(page);
    }

    @GetMapping("/{azId}")
    public AjaxResult get(@PathVariable String azId){
        return AjaxResult.success(opsAzService.getById(azId));
    }

    @PutMapping("/{azId}")
    public AjaxResult edit(@PathVariable String azId,
                           @Validated @RequestBody OpsAzEntity az) {
        az.setAzId(azId);
        return toAjax(opsAzService.updateById(az));
    }

    @DeleteMapping("/{azId}")
    public AjaxResult del(@PathVariable String azId) {
        return toAjax(opsAzService.removeById(azId));
    }

    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<OpsAzEntity> azList = opsAzService.list();
        return AjaxResult.success(azList);
    }
}
