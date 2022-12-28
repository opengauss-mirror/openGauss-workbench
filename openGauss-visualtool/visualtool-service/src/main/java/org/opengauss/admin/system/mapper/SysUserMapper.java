package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * User Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * Query users by condition
     *
     * @param queryWrapper queryWrapper
     */
    public SysUser selectUserVo(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * Query user list by pagination according to conditions
     *
     * @param page    page
     * @param sysUser sysUser
     */
    public IPage<SysUser> selectUserList(IPage<SysUser> page, @Param("entity") SysUser sysUser);

}
