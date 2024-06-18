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
 * OauthLoginUserService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/oauth/OauthLoginUserService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.oauth;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.constants.MainConstants;
import org.opengauss.admin.plugin.domain.entity.SsoUser;
import org.opengauss.admin.plugin.domain.entity.UserMapping;
import org.opengauss.admin.plugin.enums.SsoUserRoleEnum;
import org.opengauss.admin.plugin.exception.OauthLoginException;
import org.opengauss.admin.plugin.service.SsoUserService;
import org.opengauss.admin.plugin.service.UserMappingService;
import org.opengauss.admin.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @date 2024/5/31 21:08
 * @since 0.0
 */
@Service
@Slf4j
public class OauthLoginUserService {
    @Autowired
    private SsoUserService ssoUserService;

    @Autowired
    private UserMappingService userMappingService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private ISysUserService sysUserService;

    /**
     * check whether the sso user already exists
     *
     * @param ssoUser ssoUser
     * @return boolean LoginUserExists
     */
    public boolean isOauthLoginUserExists(SsoUser ssoUser) {
        List<SsoUser> ssoUsers = ssoUserService.getListByUiidAndSsoServerUrl(ssoUser);
        if (ssoUsers == null || ssoUsers.isEmpty()) {
            return false;
        }
        if (ssoUsers.size() > 1) {
            deleteOauthLoginUserWithSsoUserUiidAndSsoServer(ssoUser);
            return false;
        }
        if (!checkOauthLoginInfo(ssoUsers.get(0))) {
            return false;
        }
        refreshOauthLoginUser(ssoUser);
        return true;
    }

    /**
     * check whether the oauth login information already exists
     *
     * @param ssoUser ssoUser
     * @return boolean oauth login information exists
     */
    private boolean checkOauthLoginInfo(SsoUser ssoUser) {
        List<UserMapping> userMappings = userMappingService.getListBySsoUserIds(
                Collections.singletonList(ssoUser.getId()));
        if (userMappings == null || userMappings.isEmpty()) {
            ssoUserService.deleteSsoUserBySsoUserUiidAndSsoServerUrl(ssoUser);
            return false;
        }
        if (userMappings.size() > 1) {
            deleteOauthLoginUserWithSsoUserUiidAndSsoServer(ssoUser);
            return false;
        }
        UserMapping userMapping = userMappings.get(0);
        SysUser sysUser = sysUserService.getById(userMapping.getSysUserId());
        if (sysUser == null) {
            deleteOauthLoginUserWithSsoUserUiidAndSsoServer(ssoUser);
            return false;
        }
        if (userMapping.getSysUserId() != 1 && !sysUser.getRemark().equals(generateSysUserRemark(ssoUser))) {
            deleteOauthLoginUserWithSsoUserUiidAndSsoServer(ssoUser);
            return false;
        }
        return true;
    }

    /**
     * refresh the sso username and userNick
     *
     * @param ssoUser ssoUser
     */
    private void refreshOauthLoginUser(SsoUser ssoUser) {
        SsoUser ssoUserInDb = ssoUserService.getOneByUiidAndSsoServerUrl(ssoUser);
        boolean refreshName = false;
        boolean refreshNick = false;
        if (!ssoUserInDb.getName().equals(ssoUser.getName())) {
            ssoUserInDb.setName(ssoUser.getName());
            refreshName = true;
        }
        if (!ssoUserInDb.getNickname().equals(ssoUser.getNickname())) {
            ssoUserInDb.setNickname(ssoUser.getNickname());
            refreshNick = true;
        }
        if (refreshName || refreshNick) {
            ssoUserService.updateById(ssoUserInDb);
        }

        UserMapping userMapping = userMappingService.getOneBySsoUserId(ssoUserInDb.getId());
        if (userMapping.getSysUserId() != 1) {
            SysUser sysUser = sysUserService.getById(userMapping.getSysUserId());
            if (refreshName) {
                sysUser.setUserName(generateSysUserName(ssoUser.getName()));
                sysUser.setRemark(generateSysUserRemark(ssoUser));
            }
            if (refreshNick) {
                sysUser.setNickName(generateSysUserNickName(ssoUser.getNickname()));
            }
            if (refreshName || refreshNick) {
                sysUser.setDelFlag(null);
                sysUser.setUpdatePwd(null);
                sysUserService.updateById(sysUser);
            }
        }
    }

    /**
     * save oauth login user information to database
     *
     * @param ssoUser ssoUser
     */
    public void addOauthLoginUser(SsoUser ssoUser) {
        // To ensure the uniqueness of the user name, delete an existing user with the same name from the ssoServer.
        deleteOauthLoginUserWithSameSsoUserNameAndSsoServer(ssoUser);
        // Save SsoUser
        ssoUserService.save(ssoUser);
        SsoUser ssoUserInDb = ssoUserService.getOneByUiidAndSsoServerUrl(ssoUser);

        // Save UserMapping
        UserMapping userMapping = new UserMapping();
        if (ssoUser.getRole().equals(SsoUserRoleEnum.USER.getRole())) {
            // Delete possible sysusers with the same ssoServer name as ssoUserName.
            deleteSysUserByRemark(ssoUser);
            // Create and save SysUser.
            SysUser sysUser = createSysUser(ssoUser);
            saveSysUser(sysUser);
            // Queries the SysUser that has been added to the library.
            SysUser sysUserInDb = sysUserService.selectUserByUserName(sysUser.getUserName());
            // Example Set the sysUserId of UserMapping.
            userMapping.setSysUserId(sysUserInDb.getUserId());
        } else if (ssoUser.getRole().equals(SsoUserRoleEnum.ADMIN.getRole())
                && ssoUser.getName().equals(SsoUserRoleEnum.ADMIN.getUsername())){
            // Set directly to the DataKit administrator user.
            userMapping.setSysUserId(1);
        } else {
            String errorMsg = String.format("The sso user role is incorrect. Role :{%s}", ssoUser.getRole());
            log.error(errorMsg);
            throw new OauthLoginException(errorMsg);
        }

        userMapping.setSsoUserId(ssoUserInDb.getId());
        // Before adding the userMapping,
        // delete the mapping that may exist for the new ssoUserid and the mapping that exists for the new sysUserId.
        userMappingService.deleteUserMappingBySsoUserIds(Collections.singletonList(userMapping.getSsoUserId()));
        userMappingService.deleteUserMappingBySysUserIds(Collections.singletonList(userMapping.getSysUserId()));
        // Save UserMapping
        userMappingService.save(userMapping);
    }

    /**
     * delete oauth login user with sso user uiid and sso server url
     *
     * @param ssoUser ssoUser
     */
    private void deleteOauthLoginUserWithSsoUserUiidAndSsoServer(SsoUser ssoUser) {
        deleteOauthLoginUserWithSsoUsers(ssoUserService.deleteSsoUserBySsoUserUiidAndSsoServerUrl(ssoUser));
    }

    /**
     * delete oauth login user with same sso username and sso server url
     *
     * @param ssoUser ssoUser
     */
    private void deleteOauthLoginUserWithSameSsoUserNameAndSsoServer(SsoUser ssoUser) {
        deleteOauthLoginUserWithSsoUsers(ssoUserService.deleteSsoUserByUserNameAndSsoServerUrl(ssoUser));
    }

    /**
     * delete oauth login user with sso user list
     *
     * @param ssoUsers List<SsoUser>
     */
    private void deleteOauthLoginUserWithSsoUsers(List<SsoUser> ssoUsers) {
        if (ssoUsers.isEmpty()) {
            return;
        }
        List<Integer> ssoUserIds = ssoUsers.stream()
                .map(SsoUser::getId).collect(Collectors.toList());
        List<UserMapping> userMappings = userMappingService.deleteUserMappingBySsoUserIds(ssoUserIds);

        if (userMappings.isEmpty()) {
            return;
        }
        List<Integer> mappingSysUserIds = userMappings.stream()
                .map(UserMapping::getSysUserId)
                .filter(sysUserId -> sysUserId != 1)
                .collect(Collectors.toList());
        sysUserService.removeBatchByIds(mappingSysUserIds);
    }

    /**
     * delete sysUser by remark
     *
     * @param ssoUser ssoUser
     */
    public void deleteSysUserByRemark(SsoUser ssoUser) {
        List<SysUser> sysUsers = sysUserService.listUserByRemark(generateSysUserRemark(ssoUser));
        if (sysUsers != null && !sysUsers.isEmpty()) {
            List<Integer> sysUserIds = sysUsers.stream().map(SysUser::getUserId).collect(Collectors.toList());
            userMappingService.deleteUserMappingBySysUserIds(sysUserIds);
            sysUserService.removeBatchByIds(sysUserIds.stream()
                .filter(userId -> userId != 1).collect(Collectors.toList()));
        }
    }

    /**
     * get sysUser by ssoUser
     *
     * @param ssoUser ssoUser
     * @return SysUser
     */
    public SysUser getSysUser(SsoUser ssoUser) {
        SsoUser ssoUserInDb = ssoUserService.getOneByUiidAndSsoServerUrl(ssoUser);
        UserMapping userMapping = userMappingService.getOneBySsoUserId(ssoUserInDb.getId());
        return sysUserService.getById(userMapping.getSysUserId());
    }

    /**
     * generate sysUser entity by sso user information
     *
     * @param ssoUser ssoUser
     * @return SysUser
     */
    public SysUser createSysUser(SsoUser ssoUser) {
        SysUser sysUser = new SysUser();
        sysUser.setUserName(generateSysUserName(ssoUser.getName()));
        sysUser.setPassword(UUID.randomUUID().toString());
        sysUser.setNickName(generateSysUserNickName(ssoUser.getNickname()));
        sysUser.setPhonenumber(generateSysUserPhoneNumber());
        sysUser.setStatus("0");
        sysUser.setRemark(generateSysUserRemark(ssoUser));
        return sysUser;
    }

    /**
     * generate sysUserName by ssoUserName
     *
     * @param ssoUserName ssoUserName
     * @return String sysUserName
     */
    public String generateSysUserName(String ssoUserName) {
        String shortenedSsoUserName = ssoUserName;
        String ssoSuffix = "_SSO_";
        int maxLength = MainConstants.MAX_NAME_LENGTH - ssoSuffix.length() - ssoSuffix.length();
        if (ssoUserName.length() > maxLength) {
            shortenedSsoUserName = ssoUserName.substring(0, maxLength);
        }
        String sysUserName = shortenedSsoUserName + ssoSuffix + UUID.randomUUID().toString().substring(0, 6);
        while (UserConstants.NOT_UNIQUE.equals(sysUserService.checkUserNameUnique(sysUserName))) {
            sysUserName = shortenedSsoUserName + ssoSuffix + UUID.randomUUID().toString().substring(0, 6);
        }
        return sysUserName;
    }

    /**
     * generate sysUserNickName by ssoNickName
     *
     * @param ssoNickName ssoNickName
     * @return String sysUserNickName
     */
    public String generateSysUserNickName(String ssoNickName) {
        if (ssoNickName.length() > MainConstants.MAX_NAME_LENGTH) {
            return ssoNickName.substring(0, MainConstants.MAX_NAME_LENGTH);
        }
        return ssoNickName;
    }

    /**
     * randomly generate a phone number
     *
     * @return String sysUserPhoneNumber
     */
    public String generateSysUserPhoneNumber() {
        // Range for generating random numbers.
        final String numbers = "0123456789";
        // An object that generates random numbers.
        Random random = new Random();
        // Used to store the generated string.
        StringBuilder sb = new StringBuilder();
        sb.append("1");

        for (int i = 0; i < MainConstants.PHONE_NUMBER_LENGTH - 1; i++) {
            // Generates a random index ranging from 0 to the length of the numbers string -1.
            int index = random.nextInt(numbers.length());
            // Retrieves the corresponding character according to the index and appends it to the resulting string.
            sb.append(numbers.charAt(index));
        }

        return sb.toString();
    }

    /**
     * generate sysUserRemark by sso user information
     *
     * @param ssoUser ssoUser
     * @return String sysUserRemark
     */
    public String generateSysUserRemark(SsoUser ssoUser) {
        String remark = "DevKit mapping user. DevKit username:{" + ssoUser.getName() + "}."
                + " DevKit url:{" + ssoUser.getSsoServerUrl() + "}.";
        if (remark.length() - MainConstants.MAX_REMARK_LENGTH > 0) {
            remark = remark.substring(remark.length() - MainConstants.MAX_REMARK_LENGTH);
        }
        return remark;
    }

    /**
     * save sysUser to database
     *
     * @param user sysUser
     */
    public void saveSysUser(SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(sysUserService.checkUserNameUnique(user.getUserName()))) {
            // Duplicate user name.
            throw new OauthLoginException(ResponseCode.USER_ACCOUNT_EXISTS_ERROR.msg());
        }
        while (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(sysUserService.checkPhoneUnique(user))) {
            // If the phone number is the same, it is generated again.
            generateSysUserPhoneNumber();
        }
        if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(sysUserService.checkEmailUnique(user))) {
            // Duplicate email.
            throw new OauthLoginException(ResponseCode.USER_EMAIL_EXISTS_ERROR.msg());
        }
        if (user.getPhonenumber().length() > MainConstants.PHONE_NUMBER_LENGTH) {
            // The length of the phone number is greater than 11.
            throw new OauthLoginException(ResponseCode.USER_TELEPHONE_MAX_LENGTH_ERROR.msg());
        }
        if (user.getUserName().length() > MainConstants.MAX_NAME_LENGTH) {
            // The userName contains more than 30 characters.
            throw new OauthLoginException(ResponseCode.USER_ACCOUNT_EXISTS_ERROR.msg());
        }
        if (user.getNickName().length() > MainConstants.MAX_NAME_LENGTH) {
            // The length of user nickname is greater than 30.
            throw new OauthLoginException(ResponseCode.USER_NICKNAME_MAX_LENGTH_ERROR.msg());
        }
        if (StringUtils.isNotEmpty(user.getRemark()) && user.getRemark().length() > MainConstants.MAX_REMARK_LENGTH) {
            // The user remarks are not empty and greater than 200.
            throw new OauthLoginException(ResponseCode.USER_REMARK_MAX_LENGTH_ERROR.msg());
        }
        user.setCreateBy("admin");
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        sysUserService.insertUser(user);
    }
}
