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
 * AutoFillMetaObjectHandler.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/config/AutoFillMetaObjectHandler.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.framework.web.service.TokenService;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @className: AutoFillMetaObjectHandler
 * @description: Autofill public attribute data
 * @author: xielibo
 * @date: 2022-08-06 10:36 PM
 **/
@Slf4j
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private TokenService tokenService;

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser loginUser = null;
        try {
            loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        } catch (Exception e) {
            log.info("default user information not found, this is a new installation");
        }

        if (loginUser != null) {
            this.setFieldValByName("createBy", loginUser.getUser().getUserName(), metaObject);
            this.setFieldValByName("updateBy", loginUser.getUser().getUserName(), metaObject);
        }
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }


    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (loginUser != null) {
            this.setFieldValByName("updateBy", loginUser.getUser().getUserName(), metaObject);
        }
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
