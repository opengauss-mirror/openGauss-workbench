/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
