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
 * TaskParamController.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/controller/TaskParamController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.MigrationTaskInitGlobalParam;
import org.opengauss.admin.plugin.service.MigrationTaskInitGlobalParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@RestController
@RequestMapping("/param")
public class TaskParamController extends BaseController {

    @Autowired
    private MigrationTaskInitGlobalParamService taskInitGlobalParamService;

    /**
     * getById
     */
    @GetMapping(value = "/default")
    public AjaxResult getDefaultGlobalParam() {
        LambdaQueryWrapper<MigrationTaskInitGlobalParam> wrapper = new LambdaQueryWrapper();
        wrapper.orderByAsc(MigrationTaskInitGlobalParam::getId);
        wrapper.eq(MigrationTaskInitGlobalParam::getDbType, DbTypeEnum.MYSQL);
        List<MigrationTaskInitGlobalParam> paramList = taskInitGlobalParamService.list(wrapper);
        paramList.forEach(param -> {
            if (param.getDefaultParamValue() == null) {
                param.setDefaultParamValue(param.getParamValue());
            }
        });
        return AjaxResult.success(paramList);
    }

    /**
     * get pgsql migration config params
     *
     * @return pgsql migration config params
     */
    @GetMapping(value = "/pgsql")
    public AjaxResult getPgsqlMigrationConfigParams() {
        List<MigrationTaskInitGlobalParam> paramVos = taskInitGlobalParamService.getPgsqlMigrationConfigParams();
        paramVos.forEach(param -> param.setDefaultParamValue(param.getParamValue()));
        return AjaxResult.success(paramVos);
    }
}
