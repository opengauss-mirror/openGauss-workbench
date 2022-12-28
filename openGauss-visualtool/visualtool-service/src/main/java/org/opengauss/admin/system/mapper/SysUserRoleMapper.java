package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * User Role Relation Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * Delete user and role associations by user ID
     *
     * @param userId userId
     */
    public int deleteUserRoleByUserId(Integer userId);

    /**
     * Add user role information in batches
     *
     * @param userRoleList userRoleList
     */
    public int batchUserRole(List<SysUserRole> userRoleList);
}
