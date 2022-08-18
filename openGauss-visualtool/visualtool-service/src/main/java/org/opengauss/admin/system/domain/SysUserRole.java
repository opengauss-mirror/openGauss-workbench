package org.opengauss.admin.system.domain;

import lombok.Data;

/**
 * User Role Relation Model
 *
 * @author xielibo
 */
@Data
public class SysUserRole {

    /**
     * userId
     */
    private Integer userId;

    /**
     * roleId
     */
    private Integer roleId;
}
