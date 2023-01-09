package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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


    @NotBlank(message = "Display sort cannot be empty")
    public String getRoleSort() {
        return roleSort;
    }
}
