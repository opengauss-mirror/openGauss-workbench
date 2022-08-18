package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Menu Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * selectMenuPerms
     *
     */
    public List<String> selectMenuPerms();

    /**
     * Query the user system menu list
     *
     * @param menu menu
     */
    public List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     * Query permissions based on user ID
     *
     * @param userId userId
     */
    public List<String> selectMenuPermsByUserId(Integer userId);

    /**
     * Query menu by user ID
     *
     * @param userId userId
     */
    public List<SysMenu> selectMenuTreeByUserId(Integer userId);

    /**
     * Query menu tree information based on role ID
     *
     * @param roleId            roleId
     * @param menuCheckStrictly menuCheckStrictly
     */
    public List<Integer> selectMenuListByRoleId(@Param("roleId") Integer roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    /**
     * According to the plugin, get the parent menu that has other plugin submenus
     * @param pluginId
     * @return
     */
    public Integer countParentMenuHasOtherPluginSubmenusByPluginId(@Param("pluginId") String pluginId);
}
