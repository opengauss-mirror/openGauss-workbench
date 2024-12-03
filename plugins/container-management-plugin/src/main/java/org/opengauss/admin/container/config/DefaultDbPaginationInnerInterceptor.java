/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * DefaultDbPaginationInnerInterceptor.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/config
 * /DefaultDbPaginationInnerInterceptor.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.PostgreDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.apache.ibatis.executor.Executor;

/**
 * 分页插件
 *
 * @since 2024-08-29
 */
public class DefaultDbPaginationInnerInterceptor extends PaginationInnerInterceptor {
    @Override
    protected IDialect findIDialect(Executor executor) {
        DbType dbType = JdbcUtils.getDbType(executor);
        if (DbType.OTHER.getDb().equals(dbType.getDb())) {
            super.setDialect(new PostgreDialect());
            return super.getDialect();
        }
        return DialectFactory.getDialect(JdbcUtils.getDbType(executor));
    }
}
