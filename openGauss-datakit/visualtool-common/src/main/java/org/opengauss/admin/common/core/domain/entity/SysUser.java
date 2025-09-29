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
 * SysUser.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/SysUser.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.opengauss.admin.common.core.domain.BaseEntity;

import java.util.List;

/**
 * SysUser Model
 *
 * @author xielibo
 */
@Data
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;
    /**
     * userId
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;
    /**
     * userName
     */
    private String userName;
    /**
     * nickName
     */
    private String nickName;
    /**
     * email
     */
    private String email;
    /**
     * phonenumber
     */
    private String phonenumber;
    /**
     * sex
     */
    private String sex;
    /**
     * avatar
     */
    private String avatar;
    /**
     * password
     */
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String password;
    /**
     * salt
     */
    @TableField(exist = false)
    private String salt;
    /**
     * status(0:normal 1:disabled)
     */
    private String status;
    /**
     * delFlag(0 No 1 Yes)
     */
    @TableLogic
    private String delFlag;
    /**
     * updatePwd (0 No 1 Yes)
     */
    private String updatePwd;
    /**
     * loginIp
     */
    @TableField(fill = FieldFill.INSERT)
    private String loginIp;
    /**
     * roles
     */
    @TableField(exist = false)
    private List<SysRole> roles;
    /**
     * roleIds
     */
    @TableField(exist = false)
    private Integer roleIds;
    @TableField(exist = false)
    private String roleName;

    public SysUser() {

    }

    public SysUser(Integer userId) {
        this.userId = userId;
    }

    public static boolean isAdmin(Integer userId) {
        return userId != null && userId.equals(1);
    }

    public static Integer getAdminUserId() {
        return 1;
    }

    public boolean isAdmin() {
        return isAdmin(userId);
    }

    @Size(min = 0, max = 30, message = "User nickname length cannot exceed 30 characters")
    public String getNickName() {
        return nickName;
    }


    @NotBlank(message = "User account cannot be empty")
    @Size(min = 0, max = 30, message = "User account length cannot exceed 30 characters")
    public String getUserName() {
        return userName;
    }


    @Email(message = "E-mail format is incorrect")
    @Size(min = 0, max = 50, message = "E-mail length cannot exceed 50 characters")
    public String getEmail() {
        return email;
    }


    @Size(min = 0, max = 11, message = "Mobile phone number length cannot exceed 11 characters")
    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
