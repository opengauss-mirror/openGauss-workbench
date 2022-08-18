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
            log.error("user information not obtained");
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
