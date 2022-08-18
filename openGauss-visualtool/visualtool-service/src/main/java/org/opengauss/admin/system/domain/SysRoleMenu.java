package org.opengauss.admin.system.domain;

import lombok.Data;

/**
 * Role Menu Relation Model
 *
 * @author xielibo
 */
@Data
public class SysRoleMenu {
    /**
     * roleId
     */
    private Integer roleId;

    /**
     * menuId
     */
    private Integer menuId;
}
