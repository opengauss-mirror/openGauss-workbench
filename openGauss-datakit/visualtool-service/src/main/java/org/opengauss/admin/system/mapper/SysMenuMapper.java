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
 * SysMenuMapper.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/mapper/SysMenuMapper.java
 *
 * -------------------------------------------------------------------------
 */


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
     *
     * @param pluginId pluginId
     * @return parentMenu num
     */
    Integer countParentMenuHasOtherPluginSubmenusByPluginId(@Param("pluginId") String pluginId);

    /**
     * According to the plugin, get the parent menu that has other enable plugin submenus
     *
     * @param pluginId pluginId
     * @return parentMenu num
     */
    Integer countParentMenuHasOtherEnablePluginSubmenusByPluginId(@Param("pluginId") String pluginId);

    /**
     * Obtain the parent menu ID based on the plugin ID
     *
     * @param pluginId pluginId
     * @return parent menuIDs
     */
    List<Integer> selectParentMenuIdByPluginId(@Param("pluginId") String pluginId);
}
