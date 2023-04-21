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
 * AutowiredTypeDefinerImpl.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/config/AutowiredTypeDefinerImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.config;

import com.gitee.starblues.bootstrap.AutowiredTypeDefinerConfig;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.realize.AutowiredTypeDefiner;

import javax.sql.DataSource;

/**
 * @className: AutowiredTypeDefinerImpl
 * @description: AutowiredTypeDefinerImpl
 * @author: xielibo
 * @date: 2022-10-28 13:43
 **/
public class AutowiredTypeDefinerImpl implements AutowiredTypeDefiner {

    @Override
    public void config(AutowiredTypeDefinerConfig config) {
        config.add(AutowiredType.Type.MAIN, DataSource.class);
    }
}
