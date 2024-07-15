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
 * SsoUser.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/domain/entity/SsoUser.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.plugin.vo.UserinfoResponseBody;

/**
 * @date 2024/5/31 20:37
 * @since 0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("oauth_login_sso_user")
public class SsoUser {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String uiid;
    private String name;
    private String nickname;
    private String role;
    private String ssoServerUrl;

    /**
     * constructor
     *
     * @param userinfoResponseBody userinfoResponseBody
     * @param ssoServerUrl ssoServerUrl
     */
    public SsoUser(UserinfoResponseBody userinfoResponseBody, String ssoServerUrl) {
        this.name = userinfoResponseBody.getName();
        this.uiid = userinfoResponseBody.getUiid();
        this.nickname = userinfoResponseBody.getNickname();
        this.role = userinfoResponseBody.getRole();
        this.ssoServerUrl = ssoServerUrl;
    }

}
