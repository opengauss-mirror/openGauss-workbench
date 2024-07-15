/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * SsoUserService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/SsoUserService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.SsoUser;

import java.util.List;

/**
 * @date 2024/6/7 11:39
 * @since 0.0
 */
public interface SsoUserService extends IService<SsoUser> {

    /**
     * getOneByUiidAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return SsoUser
     */
    public abstract SsoUser getOneByUiidAndSsoServerUrl(SsoUser ssoUser);

    /**
     * getOneByUiidAndSsoServerUrl
     *
     * @param uuid uuid
     * @param ssoServerUrl ssoServerUrl
     * @return SsoUser
     */
    public abstract SsoUser getOneByUiidAndSsoServerUrl(String uuid, String ssoServerUrl);

    /**
     * getListByUiidAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    public abstract List<SsoUser> getListByUiidAndSsoServerUrl(SsoUser ssoUser);

    /**
     * getListByUserNameAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    public abstract List<SsoUser> getListByUserNameAndSsoServerUrl(SsoUser ssoUser);

    /**
     * deleteSsoUsers
     *
     * @param ssoUsers List<SsoUser>
     */
    public abstract void deleteSsoUsers(List<SsoUser> ssoUsers);

    /**
     * deleteSsoUserBySsoUserUiidAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    public abstract List<SsoUser> deleteSsoUserBySsoUserUiidAndSsoServerUrl(SsoUser ssoUser);

    /**
     * deleteSsoUserByUserNameAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    public abstract List<SsoUser> deleteSsoUserByUserNameAndSsoServerUrl(SsoUser ssoUser);
}
