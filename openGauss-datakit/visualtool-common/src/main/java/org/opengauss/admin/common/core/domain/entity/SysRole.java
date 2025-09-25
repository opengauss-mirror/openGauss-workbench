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
 * SysRole.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/SysRole.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Role Model
 *
 * @author xielibo
 */
@Data
public class SysRole extends BaseEntity {
    private static final long serialVersionUID = 1L;


    public SysRole() {
    }

    public SysRole(Integer roleId) {
        this.roleId = roleId;
    }

    /**
     * roleId
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Integer roleId;

    /**
     * roleName
     */
    @NotBlank(message = "roleName cannot be empty")
    @Size(min = 0, max = 25, message = "roleName length cannot exceed 25 characters")
    private String roleName;

    /**
     * roleKey
     */
    private String roleKey;

    /**
     * roleSort
     */
    private String roleSort;

    /**
     * dataScope
     */
    private String dataScope;

    /**
     * menuCheckStrictly
     */
    private boolean menuCheckStrictly;

    /**
     * deptCheckStrictly
     */
    private boolean deptCheckStrictly;

    /**
     * status
     */
    private String status;

    /**
     * delFlag
     */
    @TableLogic
    private String delFlag;

    @TableField(exist = false)
    private boolean flag = false;

    @TableField(exist = false)
    private Integer[] menuIds;


    public boolean isAdmin() {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Integer roleId) {
        return roleId != null && 1 == roleId;
    }

    public String getRoleName() {
        return roleName;
    }


    public String getRoleKey() {
        return roleKey;
    }


    public String getRoleSort() {
        return roleSort;
    }
}
