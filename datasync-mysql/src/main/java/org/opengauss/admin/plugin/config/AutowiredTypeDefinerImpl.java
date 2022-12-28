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
