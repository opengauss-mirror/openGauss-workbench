package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Role Service Interface
 *
 * @author xielibo
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * Query role data by pagination according to conditions
     *
     */
    public IPage<SysRole> selectRoleList(SysRole sysRole, IPage<SysRole> page);

    /**
     * Query roles based on user ID
     * @param userId userId
     */
    List<SysRole> selectRolesByUserId(Integer userId);

    /**
     * Query role data based on conditions
     *
     * @param sysRole role
     */
    public List<SysRole> selectRoleList(SysRole sysRole);


    /**
     * Query permissions based on user ID
     *
     * @param userId userId
     */
    public Set<String> selectRolePermissionByUserId(Integer userId);

    /**
     * Query All Role
     *
     */
    public List<SysRole> selectRoleAll();

    /**
     * Get the role selection box list according to the user ID
     *
     * @param userId userId
     */
    public List<String> selectRoleListByUserId(Integer userId);

    /**
     * Query roles by role ID
     *
     */
    public SysRole selectRoleById(Integer roleId);

    /**
     * Check if the role name is unique
     *
     * @param role role
     */
    public String checkRoleNameUnique(SysRole role);

    /**
     * Check if the role allows the operation
     *
     */
    public void checkRoleAllowed(SysRole role);

    /**
     * Query the number of roles used by the role ID
     *
     */
    public int countUserRoleByRoleId(Integer roleId);

    /**
     * Save Role
     *
     */
    public int insertRole(SysRole role);

    /**
     * Update Role
     *
     * @param role role
     */
    public int updateRole(SysRole role);

    /**
     * Update Status
     *
     * @param role role
     */
    public int updateRoleStatus(SysRole role);

    @Transactional(rollbackFor = Exception.class)
    int authDataScope(SysRole role);

    @Transactional(rollbackFor = Exception.class)
    int deleteRoleByIds(Integer[] roleIds);
}
