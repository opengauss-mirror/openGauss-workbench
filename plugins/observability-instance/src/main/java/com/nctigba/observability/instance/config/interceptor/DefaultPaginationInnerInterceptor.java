/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DefaultPaginationInnerInterceptor.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/config/interceptor/DefaultPaginationInnerInterceptor.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.config.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.PostgreDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.apache.ibatis.executor.Executor;

/**
 * Override PaginationInnerInterceptor.findIDialect
 *
 * @author liupengfei
 * @since 2023/12/14
 */
public class DefaultPaginationInnerInterceptor extends PaginationInnerInterceptor {
    @Override
    protected IDialect findIDialect(Executor executor) {
        DbType dbType = JdbcUtils.getDbType(executor);
        if(DbType.OTHER.getDb().equals(dbType.getDb())) {
            super.setDialect(new PostgreDialect());
            return super.getDialect();
        }
        return DialectFactory.getDialect(JdbcUtils.getDbType(executor));
    }
}
