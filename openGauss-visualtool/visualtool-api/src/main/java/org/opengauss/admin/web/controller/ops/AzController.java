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
 * AzController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/AzController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
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

    @GetMapping("/hasName")
    public AjaxResult hasName(@RequestParam("name") String name) {
        return AjaxResult.success(opsAzService.hasName(name));
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
        LambdaQueryWrapper<OpsAzEntity> queryWrapper = Wrappers.lambdaQuery(OpsAzEntity.class)
                .eq(OpsAzEntity::getName, az.getName())
                .ne(OpsAzEntity::getAzId, azId);

        if (opsAzService.count(queryWrapper) > 0) {
            throw new OpsException("name already exists");
        }

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
