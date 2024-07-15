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
 * UserMappingService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/UserMappingService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.UserMapping;

import java.util.List;

/**
 * @date 2024/6/7 11:43
 * @since 0.0
 */
public interface UserMappingService extends IService<UserMapping> {

    /**
     * getOneBySsoUserId
     *
     * @param ssoUserId ssoUserId
     * @return .UserMapping
     */
    public abstract UserMapping getOneBySsoUserId(Integer ssoUserId);

    /**
     * getListBySsoUserIds
     *
     * @param ssoUserIds ssoUserIds
     * @return List<UserMapping>
     */
    public abstract List<UserMapping> getListBySsoUserIds(List<Integer> ssoUserIds);

    /**
     * getListBySysUserIds
     *
     * @param sysUserIds sysUserIds
     * @return List<UserMapping>
     */
    public abstract List<UserMapping> getListBySysUserIds(List<Integer> sysUserIds);

    /**
     * deleteUserMappings
     *
     * @param userMappings List<UserMapping>
     */
    public abstract void deleteUserMappings(List<UserMapping> userMappings);

    /**
     * deleteUserMappingBySsoUserIds
     *
     * @param ssoUserIds ssoUserIds
     * @return List<UserMapping>
     */
    public abstract List<UserMapping> deleteUserMappingBySsoUserIds(List<Integer> ssoUserIds);

    /**
     * deleteUserMappingBySysUserIds
     *
     * @param integers sysUserIds
     * @return List<UserMapping>
     */
    public abstract List<UserMapping> deleteUserMappingBySysUserIds(List<Integer> integers);
}
