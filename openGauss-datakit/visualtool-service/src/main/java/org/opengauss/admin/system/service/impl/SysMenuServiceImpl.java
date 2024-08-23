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
 * SysMenuServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysMenuServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.domain.TreeSelect;
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.enums.SysLanguage;
import org.opengauss.admin.common.enums.SysMenuPluginComponent;
import org.opengauss.admin.common.enums.SysMenuStatus;
import org.opengauss.admin.common.enums.SysMenuVisible;
import org.opengauss.admin.system.domain.SysRoleMenu;
import org.opengauss.admin.common.core.vo.MetaVo;
import org.opengauss.admin.common.core.vo.RouterVo;
import org.opengauss.admin.system.mapper.SysMenuMapper;
import org.opengauss.admin.system.mapper.SysRoleMapper;
import org.opengauss.admin.system.mapper.SysRoleMenuMapper;
import org.opengauss.admin.system.service.ISysMenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * menu service
 *
 * @author xielibo
 */
@Service
@Slf4j
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    /**
     * Query the system menu list according to the user
     *
     * @param userId user ID
     * @return menu list
     */
    @Override
    public List<SysMenu> selectMenuList(Integer userId) {
        return selectMenuList(new SysMenu(), userId);
    }

    /**
     * Query menu list
     *
     * @param menu menu
     * @param userId user ID
     * @return menu list
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Integer userId) {
        List<SysMenu> menuList = null;
        if (SysUser.isAdmin(userId)) {
            LambdaQueryWrapper<SysMenu> query = new QueryWrapper<SysMenu>().lambda();
            query.like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName());
            query.eq(StringUtils.isNotEmpty(menu.getVisible()), SysMenu::getVisible, menu.getVisible());
            query.eq(StringUtils.isNotEmpty(menu.getStatus()), SysMenu::getStatus, menu.getStatus());
            query.orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum);
            menuList = menuMapper.selectList(query);
        } else {
            menu.getParams().put("userId", userId);
            menuList = menuMapper.selectMenuListByUserId(menu);
        }
        return menuList;
    }


    /**
     * get special routers
     */
    @Override
    public List<SysMenu> selectSpecialRouteList(Integer openPosition) {
        LambdaQueryWrapper<SysMenu> query = new QueryWrapper<SysMenu>().lambda();
        query.eq(SysMenu::getVisible, SysMenuVisible.HIDE.getCode());
        query.eq(openPosition != null, SysMenu::getOpenPosition, openPosition);
        query.orderByDesc(SysMenu::getCreateTime);
        return menuMapper.selectList(query);
    }

    /**
     * Determine whether the menu exists
     * @param parentId
     * @param pluginId
     * @param path
     * @param menuName
     * @return
     */
    @Override
    public SysMenu existsMenu(Integer parentId, String pluginId, String path, String menuName) {
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();
        query.eq(parentId != null, SysMenu::getParentId, parentId);
        query.eq(StringUtils.isNotBlank(pluginId), SysMenu::getPluginId, pluginId);
        query.eq(StringUtils.isNotBlank(path), SysMenu::getPath, path);
        query.eq(StringUtils.isNotBlank(menuName), SysMenu::getMenuName, menuName);
        List<SysMenu> list = menuMapper.selectList(query);
        return list.size() > 0 ? list.get(0) : null;
    }

    /**
     * Query permission list based on user ID
     *
     * @param userId user ID
     * @return permission list
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Integer userId) {
        List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * Query menu list by user ID
     *
     * @param userId user ID
     * @return menu list
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Integer userId) {
        List<SysMenu> menus = null;
        if (SysUser.isAdmin(userId)) {
            LambdaQueryWrapper q = new QueryWrapper<SysMenu>().lambda().in(SysMenu::getMenuType, UserConstants.TYPE_DIR, UserConstants.TYPE_MENU).eq(SysMenu::getStatus, UserConstants.NORMAL).orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum);
            menus = menuMapper.selectList(q);
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId);
            menus.addAll(selectSpecialRouteList(null));
        }
        return getChildPerms(menus, 0);
    }

    /**
     * Query menu tree information based on role ID
     *
     * @param roleId role ID
     * @return menu IDs
     */
    @Override
    public List<Integer> selectMenuListByRoleId(Integer roleId) {
        SysRole role = roleMapper.selectRole(new QueryWrapper<SysRole>().eq("r.role_id", roleId));
        return menuMapper.selectMenuListByRoleId(roleId, role.isMenuCheckStrictly());
    }

    /**
     * The menu needed to build the front-end routing
     *
     * @param menus menu list
     * @param language current locale
     * @return router list
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus, String language) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            List<SysMenu> cMenus = menu.getChildren();
            router.setIsPluginMenu(StringUtils.isNotBlank(menu.getPluginId()));
            if (StringUtils.isNotBlank(menu.getPluginTheme())) {
                Map<String, Object> routeQuery = Maps.newHashMap();
                routeQuery.put("theme", menu.getPluginTheme());
                router.setQuery(routeQuery);
            }
            String menuName = SysLanguage.EN.getCode().equals(language) ? menu.getMenuEnName() : menu.getMenuName();
            router.setMeta(new MetaVo(menuName, menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getOrderNum(), cMenus.size() == 0,"1".equals(menu.getVisible())));
            router.setMenuClassify(menu.getMenuClassify());
            if (!cMenus.isEmpty() && cMenus.size() > 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus,language));
            } else if (isMeunFrame(menu)) {
                router.setRedirect(menu.getPath());
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(getRouteName(menu));
                children.setMeta(new MetaVo(menuName, menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getOrderNum(), cMenus.size() == 0,"1".equals(menu.getVisible())));
                children.setIsPluginMenu(StringUtils.isNotBlank(menu.getPluginId()));
                children.setMenuClassify(menu.getMenuClassify());
                if (StringUtils.isNotBlank(menu.getPluginTheme())) {
                    Map<String, Object> routeQuery = Maps.newHashMap();
                    routeQuery.put("theme", menu.getPluginTheme());
                    children.setQuery(routeQuery);
                }
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * Build the menu tree structure required by the front end
     *
     * @param menus menus
     * @return menu tree list
     */
    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        List<Integer> tempList = new ArrayList<Integer>();
        for (SysMenu hospital : menus) {
            tempList.add(hospital.getMenuId());
        }
        for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext(); ) {
            SysMenu menu = (SysMenu) iterator.next();
            if (!tempList.contains(menu.getParentId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * Build the menu tree select structure required by the front end
     *
     * @param menus menu list
     * @return tree select
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus) {
        List<SysMenu> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * get by ID
     *
     * @param menuId Menu ID
     * @return menu
     */
    @Override
    public SysMenu selectMenuById(Integer menuId) {
        return menuMapper.selectById(menuId);
    }

    /**
     * Determine if a menu exists as a submenu
     *
     * @param menuId Menu ID
     */
    @Override
    public boolean hasChildByMenuId(Integer menuId) {
        int result = menuMapper.selectCount(new QueryWrapper<SysMenu>().lambda().eq(SysMenu::getParentId, menuId)).intValue();
        return result > 0 ? true : false;
    }

    /**
     * According to the plugin, get the parent menu that has other plugin submenus
     * @param pluginId
     * @return
     */
    @Override
    public Integer countMenuHasOtherPluginSubmenuByPluginId(String pluginId) {
        return menuMapper.countParentMenuHasOtherPluginSubmenusByPluginId(pluginId);
    }

    @Override
    public Integer countMenuHasOtherEnablePluginSubmenuByPluginId(String pluginId) {
        return menuMapper.countParentMenuHasOtherEnablePluginSubmenusByPluginId(pluginId);
    }

    /**
     * Determine whether the menu is assigned
     *
     * @param menuId Menu ID
     */
    @Override
    public boolean checkMenuExistRole(Integer menuId) {
        int result = roleMenuMapper.selectCount(new QueryWrapper<SysRoleMenu>().lambda().eq(SysRoleMenu::getMenuId, menuId)).intValue();
        return result > 0 ? true : false;
    }

    /**
     * save menu
     */
    @Override
    public int insertMenu(SysMenu menu) {
        return menuMapper.insert(menu);
    }

    /**
     * update menu
     */
    @Override
    public int updateMenu(SysMenu menu) {
        return menuMapper.updateById(menu);
    }

    /**
     * delete menu
     */
    @Override
    public int deleteMenuById(Integer menuId) {
        return menuMapper.deleteById(menuId);
    }


    /**
     * delete by pluginId
     *
     * @param pluginId pluginId
     * @return result
     */
    @Override
    public int deleteByPluginId(String pluginId) {
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();
        query.eq(StringUtils.isNotBlank(pluginId), SysMenu::getPluginId, pluginId);
        return menuMapper.delete(query);
    }

    /**
     * update menu disable by pluginId
     *
     * @param pluginId pluginId
     * @return result
     */
    @Override
    public int updateDisableByPluginId(String pluginId) {
        System.out.println("pluginId==" + pluginId);
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();
        query.eq(StringUtils.isNotBlank(pluginId), SysMenu::getPluginId, pluginId);
        SysMenu update = new SysMenu();
        update.setStatus(SysMenuStatus.DISABLE.getCode());
        return menuMapper.update(update, query);
    }

    /**
     * Check if the menu is unique
     */
    @Override
    public String checkMenuNameUnique(SysMenu menu) {
        Integer menuId = menu.getMenuId() == null ? 1 : menu.getMenuId();
        SysMenu info = menuMapper.selectOne(new QueryWrapper<SysMenu>().lambda().eq(SysMenu::getMenuName, menu.getMenuName()).eq(SysMenu::getParentId, menu.getParentId()));
        if (!Objects.isNull(info) && !info.getMenuId().equals(menuId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * get router name
     */
    public String getRouteName(SysMenu menu) {
        String[] paths = menu.getPath().split("\\/");
        StringBuffer sb = new StringBuffer();
        for (String path : paths) {
            if (StringUtils.isNotBlank(path)) {
                sb.append(StringUtils.capitalize(path));
            }
        }
        return sb.toString();
    }

    /**
     * get router path
     *
     * @param menu
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = menu.getPath();
        if (0 == menu.getParentId() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame())) {
            routerPath =  menu.getPath();
        }
        return routerPath;
    }

    /**
     * get menu component
     */
    public String getComponent(SysMenu menu) {
        String component = UserConstants.LAYOUT;
        if(!isPluginMenu(menu)){
            if (StringUtils.isNotEmpty(menu.getComponent()) && !isMeunFrame(menu)) {
                component = menu.getComponent();
            } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
                component = UserConstants.PARENT_VIEW;
            }
        } else {
            if(0 != menu.getParentId() && UserConstants.TYPE_MENU.equals(menu.getMenuType())){
                component = menu.getComponent();
            }
        }
        return component;
    }

    public boolean isMeunFrame(SysMenu menu) {
        return 0 == menu.getParentId() && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    public boolean isPluginMenu(SysMenu menu) {
        return SysMenuPluginComponent.PAGE_IFRAME.getCode().equals(menu.getComponent());
    }

    public boolean isParentView(SysMenu menu) {
        return 0 != menu.getParentId() && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * get submenu
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, Integer parentId) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = (SysMenu) iterator.next();
            if (t.getParentId().equals(parentId)) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    private void recursionFn(List<SysMenu> list, SysMenu t) {
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().equals(t.getMenuId())) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }

    /**
     * update plugin menu theme
     *
     * @param pluginId pluginId
     * @param theme theme
     */
    @Override
    public void updatePluginMenuTheme(String pluginId, String theme) {
        if (StringUtils.isNotBlank(pluginId) && StringUtils.isNotBlank(theme)) {
            SysMenu menu = new SysMenu();
            menu.setPluginTheme(theme);
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getPluginId, pluginId);
            this.update(menu, queryWrapper);
        }
    }

    /**
     * update plugin menu icon
     *
     * @param pluginId pluginId
     * @param icon icon
     */
    @Override
    public void updatePluginMenuIcon(String pluginId, String icon) {
        if (StringUtils.isNotBlank(pluginId) && StringUtils.isNotBlank(icon)) {
            SysMenu menu = new SysMenu();
            menu.setIcon(icon);
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getPluginId, pluginId);
            this.update(menu, queryWrapper);
        }
    }

    /**
     * update plugin menu icon
     *
     * @param pluginId pluginId
     * @param icon icon
     */
    @Override
    public void updatePluginFatherMenuIcon(String pluginId, String icon) {
        if (StringUtils.isNotBlank(pluginId) && StringUtils.isNotBlank(icon)) {
            List<Integer> menuIds = menuMapper.selectParentMenuIdByPluginId(pluginId);
            log.info("pluginId:{},menuids:{}", pluginId, menuIds);
            if (menuIds.isEmpty()) {
                return;
            }
            menuIds.forEach(menuId -> {
                SysMenu menu = new SysMenu();
                menu.setIcon(icon);
                LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysMenu::getMenuId, menuId);
                this.update(menu, queryWrapper);
            });
        }
    }

}
