package org.opengauss.admin.plugin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.framework.web.service.TokenService;
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

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser loginUser = null;
        try {
            loginUser = SecurityUtils.getLoginUser();
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
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            this.setFieldValByName("updateBy", loginUser.getUser().getUserName(), metaObject);
        }
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
