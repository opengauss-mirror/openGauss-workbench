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
 * HostUserController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/HostUserController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/11 23:00
 **/
@RestController
@RequestMapping("/hostUser")
public class HostUserController extends BaseController {
    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private IHostService hostService;

    @GetMapping("/page/{hostId}")
    public TableDataInfo page(@PathVariable("hostId") String hostId) {
        IPage<OpsHostUserEntity> page = hostUserService.page(startPage(), Wrappers.lambdaQuery(OpsHostUserEntity.class).eq(OpsHostUserEntity::getHostId, hostId));
        return getDataTable(page);
    }

    @GetMapping("/listAll/{hostId}")
    public AjaxResult listAll(@PathVariable("hostId") String hostId) {
        List<OpsHostUserEntity> opsHostUserEntityList = hostUserService.list(Wrappers.lambdaQuery(OpsHostUserEntity.class).eq(OpsHostUserEntity::getHostId, hostId));
        return AjaxResult.success(opsHostUserEntityList);
    }

    @GetMapping("/listAllWithoutRoot/{hostId}")
    public AjaxResult listAllWithoutRoot(@PathVariable("hostId") String hostId) {
        LambdaQueryWrapper<OpsHostUserEntity> queryWrapper = Wrappers.lambdaQuery(OpsHostUserEntity.class)
                .eq(OpsHostUserEntity::getHostId, hostId)
                .ne(OpsHostUserEntity::getUsername, "root");
        List<OpsHostUserEntity> opsHostUserEntityList = hostUserService.list(queryWrapper);
        return AjaxResult.success(opsHostUserEntityList);
    }

    @PostMapping
    public AjaxResult add(@RequestBody @Validated HostUserBody hostUserBody) {
        OpsHostEntity host = hostService.getById(hostUserBody.getHostId());
        OpsAssert.nonNull(host, "host id " + hostUserBody.getHostId() + " is invalid");
        hostUserService.add(host, hostUserBody);
        return AjaxResult.success();
    }

    @PutMapping("/{hostUserId}")
    public AjaxResult edit(@PathVariable String hostUserId,
                           @RequestBody HostUserBody hostUserBody) {
        OpsHostEntity host = hostService.getById(hostUserBody.getHostId());
        OpsAssert.nonNull(host, "host id " + hostUserBody.getHostId() + " is invalid");
        hostUserService.edit(host, hostUserId, hostUserBody);
        return AjaxResult.success();
    }

    @DeleteMapping("/{hostUserId}")
    public AjaxResult del(@PathVariable String hostUserId) {
        hostUserService.del(hostUserId);
        return AjaxResult.success();
    }

    /**
     * check if the user has root permission
     *
     * @param userId user ID
     * @return has root permission or not
     */
    @GetMapping("/hasRootPermission/{userId}")
    public AjaxResult hasRootPermission(@PathVariable("userId") String userId) {
        OpsHostUserEntity hostUser = hostUserService.getById(userId);
        OpsAssert.nonNull(hostUser, "host user id " + userId + " is invalid");

        OpsHostEntity host = hostService.getById(hostUser.getHostId());
        OpsAssert.nonNull(host, "user hostId " + hostUser.getHostId() + " is invalid");
        boolean hasPermission = hostUserService.hasRootPermission(host, userId);
        return AjaxResult.success(hasPermission);
    }
}
