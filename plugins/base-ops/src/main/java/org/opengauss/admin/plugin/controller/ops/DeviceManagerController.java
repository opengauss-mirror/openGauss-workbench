/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDeviceManagerEntity;
import org.opengauss.admin.plugin.service.ops.IOpsDeviceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Operation and maintenance cluster operations
 *
 * @author lhf
 * @since 2022/8/6 17:38
 **/
@RestController
@RequestMapping("/deviceManager")
public class DeviceManagerController extends BaseController {
    @Autowired
    private IOpsDeviceManagerService opsDeviceManagerService;

    /**
     * query repeat device manager name
     *
     * @param name name
     * @return AjaxResult
     */
    @GetMapping("/hasName")
    public AjaxResult hasName(@RequestParam("name") String name) {
        boolean has = opsDeviceManagerService.hasName(name);
        return AjaxResult.success(has);
    }

    /**
     * list all device manager
     *
     * @return AjaxResult
     */
    @GetMapping("/list")
    public AjaxResult list() {
        List<OpsDeviceManagerEntity> sessionVOList = opsDeviceManagerService.listDeviceManager();
        return AjaxResult.success(sessionVOList);
    }

    /**
     * add device manager
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return AjaxResult
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody @Validated OpsDeviceManagerEntity deviceManagerEntity) {
        opsDeviceManagerService.add(deviceManagerEntity);
        return AjaxResult.success();
    }

    /**
     * modify device manager
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return AjaxResult
     */
    @PostMapping("/modify")
    public AjaxResult modify(@RequestBody @Validated OpsDeviceManagerEntity deviceManagerEntity) {
        opsDeviceManagerService.modify(deviceManagerEntity);
        return AjaxResult.success();
    }

    /**
     * delete device manager
     *
     * @param name name
     * @return AjaxResult
     */
    @PostMapping("/delete/{name}")
    public AjaxResult delete(@PathVariable("name") String name) {
        opsDeviceManagerService.delete(name);
        return AjaxResult.success();
    }

    /**
     * test device manager connect
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return AjaxResult
     */
    @PostMapping("/connect")
    public AjaxResult connect(@RequestBody @Validated OpsDeviceManagerEntity deviceManagerEntity) {
        boolean isConnect = opsDeviceManagerService.connect(deviceManagerEntity);
        return isConnect ? AjaxResult.success() : AjaxResult.error("Connectivity test failed");
    }
}
