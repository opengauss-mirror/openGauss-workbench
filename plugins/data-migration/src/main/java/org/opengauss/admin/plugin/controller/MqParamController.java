/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;
import org.opengauss.admin.plugin.service.MigrationMqInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MQparam controller
 *
 * @author: www
 * @date: 2023/11/28 14:54
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@RestController
@RequestMapping("/MQConfig")
public class MqParamController extends BaseController {
    @Autowired
    private MigrationMqInstanceService mqConfigService;

    /**
     * getMQInstances 获取mq实例信息
     *
     * @author: www
     * @date: 2023/11/28 15:01
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param mqInstanceConfig config
     * @return TableDataInfo data
     */
    @PostMapping(value = "/list")
    public TableDataInfo listMQInstances(@RequestBody(required = false)
                                            MigrationThirdPartySoftwareConfig mqInstanceConfig) {
        IPage<MigrationThirdPartySoftwareConfig> list = mqConfigService.selectMQPage(startPage(), mqInstanceConfig);
        return getDataTable(list);
    }
}
