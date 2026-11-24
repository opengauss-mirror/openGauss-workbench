/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.config;

import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.enums.DbDataLocationEnum;

import java.util.Optional;

/**
 * DbDataLocationHolder
 *
 * @date 2025-12-11
 * @since 7.0.0-RC3
 **/
public class DbDataLocationHolder {
    /**
     * get ops plugin current database type
     *
     * @return DbDataLocationEnum
     */
    public static DbDataLocationEnum getLocationDatabase() {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        String driverName = environmentProvider.getString("spring.datasource.driver-class-name");
        Optional<DbDataLocationEnum> dbDataLocationEnum = DbDataLocationEnum.of(driverName);
        return dbDataLocationEnum.orElseThrow(() -> new OpsException("not support database location" + driverName));
    }
}
