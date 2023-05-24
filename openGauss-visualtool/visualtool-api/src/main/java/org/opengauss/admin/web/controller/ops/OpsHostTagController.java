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
 * OpsHostTagController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/OpsHostTagController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagAddInputDto;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagPageVO;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.system.service.ops.IOpsHostTagRelService;
import org.opengauss.admin.system.service.ops.IOpsHostTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lhf
 * @date 2023/3/14 23:42
 **/
@RestController
@RequestMapping("/hostTag")
public class OpsHostTagController extends BaseController {
    @Autowired
    private IOpsHostTagService opsHostTagService;
    @Autowired
    private IOpsHostTagRelService opsHostTagRelService;

    @PutMapping("/addTag")
    public AjaxResult addTag(@RequestBody HostTagInputDto hostTagInputDto) {
        opsHostTagService.addTag(hostTagInputDto);
        return AjaxResult.success();
    }

    @PutMapping("/delTag")
    public AjaxResult delTagRelation(@RequestBody HostTagInputDto hostTagInputDto) {
        opsHostTagRelService.delTagRelation(hostTagInputDto);
        return AjaxResult.success();
    }

    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<OpsHostTagEntity> list = opsHostTagService.list();
        return AjaxResult.success(list);
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(value = "name", required = false) String name) {
        IPage<HostTagPageVO> page = opsHostTagService.page(startPage(), name);
        return getDataTable(page);
    }

    @PostMapping("/add")
    public AjaxResult add(@RequestBody HostTagAddInputDto addInputDto) {
        opsHostTagService.add(addInputDto.getName());
        return AjaxResult.success();
    }

    @PutMapping("/update/{tagId}")
    public AjaxResult update(@PathVariable("tagId") String tagId, @RequestBody HostTagAddInputDto addInputDto) {
        opsHostTagService.update(tagId, addInputDto.getName());
        return AjaxResult.success();
    }

    @DeleteMapping("/del/{tagId}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult del(@PathVariable("tagId") String tagId) {
        opsHostTagRelService.delByTagId(tagId);
        opsHostTagService.removeById(tagId);
        return AjaxResult.success();
    }

}
