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
 * PluginApplication.java
 *
 * IDENTIFICATION
 * openGauss-visualtool-plugin/src/main/java/org/opengauss/admin/plugin/PluginApplication.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude={
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class PluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        new PluginApplication().run(args);
    }
}
