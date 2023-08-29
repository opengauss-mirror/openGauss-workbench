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
 * MenuFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/MenuFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.common.enums.SysMenuPluginComponent;
import org.opengauss.admin.common.enums.SysMenuPluginOpenWay;
import org.opengauss.admin.common.enums.SysMenuRouteOpenPosition;
import org.opengauss.admin.common.enums.SysMenuStatus;
import org.opengauss.admin.common.enums.SysMenuVisible;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.domain.SysPlugin;
import org.opengauss.admin.system.domain.SysPluginLogo;
import org.opengauss.admin.system.service.ISysMenuService;
import org.opengauss.admin.system.service.ISysPluginLogoService;
import org.opengauss.admin.system.service.ISysPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @className: MenuFacade
 * @description: Menu service provided to plugins.
 * @author: xielibo
 * @date: 2022-09-05 6:59 PM
 **/
@Service
@Slf4j
public class MenuFacade {

    @Autowired
    private ISysMenuService sysMenuService;
    @Autowired
    private ISysPluginService sysPluginService;
    @Autowired
    private ISysPluginLogoService sysPluginLogoService;

    private MenuVo saveMenu(String pluginId, String menuName,String menuEnName, Integer order, String path, Integer parentId, Integer openWay, String visible, Integer openPosition, String queryTemplate) {
        if (path == null) {
            path = "";
        } else {
            path = Constants.PLUGIN_PATH_PREFIX + "/" + pluginId + "/" + path;
        }
        log.info("call save menu, pluginId={}, menuName={}, path={}, parentId={}", pluginId, menuName, path, parentId);
        SysMenu menu = sysMenuService.existsMenu(parentId, null, null, menuName);
        if (menu == null) {
            menu = new SysMenu();
            menu.setVisible(visible);
            menu.setMenuName(menuName);
            menu.setMenuEnName(menuEnName);
            if (parentId == null) {
                parentId = 0;
            }
            menu.setPluginId(pluginId);
            menu.setParentId(parentId);
            menu.setOpenWay(openWay);
            menu.setPath(path);
            if (openWay == null || SysMenuPluginOpenWay.PAGE_OPEN.getCode().equals(openWay)) {
                menu.setComponent(SysMenuPluginComponent.PAGE_IFRAME.getCode());
            } else {
                menu.setComponent(SysMenuPluginComponent.WIN_IFRAME.getCode());
            }
            menu.setOrderNum(order + 10000);
            menu.setMenuType(UserConstants.TYPE_MENU);
            menu.setOpenPosition(openPosition);
            menu.setQueryTemplate(queryTemplate);
            menu.setCreateTime(new Date());
            SysPlugin plugin = sysPluginService.getByPluginId(pluginId);
            if (plugin != null) {
                if (StringUtils.isNotBlank(plugin.getTheme())) {
                    menu.setPluginTheme(plugin.getTheme());
                }
                SysPluginLogo logo = sysPluginLogoService.getByPluginId(pluginId);
                if (logo != null && StringUtils.isNotBlank(logo.getLogoPath())) {
                    menu.setIcon(logo.getLogoPath());
                }
            }
            sysMenuService.save(menu);
            if (parentId != 0) {
                SysMenu sysMenu = new SysMenu();
                sysMenu.setMenuId(parentId);
                sysMenu.setMenuType(UserConstants.TYPE_DIR);
                sysMenuService.updateMenu(sysMenu);
            }
        } else {
            menu.setStatus(SysMenuStatus.ENABLE.getCode());
            SysPlugin plugin = sysPluginService.getByPluginId(pluginId);
            if (plugin != null) {
                if (StringUtils.isNotBlank(plugin.getTheme())) {
                    menu.setPluginTheme(plugin.getTheme());
                }
                SysPluginLogo logo = sysPluginLogoService.getByPluginId(pluginId);
                if (logo != null && StringUtils.isNotBlank(logo.getLogoPath())) {
                    menu.setIcon(logo.getLogoPath());
                }
            }
            sysMenuService.updateMenu(menu);
        }
        return menu.toMenuVo();
    }

    /**
     * save menu and router
     * @param pluginId
     * @param menuName
     */
    public MenuVo savePluginMenu(String pluginId, String menuName, String menuEnName, Integer order) {
        return saveMenu(pluginId, menuName, menuEnName, order, "index", 0, SysMenuPluginOpenWay.PAGE_OPEN.getCode(), SysMenuVisible.SHOW.getCode(), null, null);
    }

    /**
     * save menu and router
     * @param pluginId
     * @param menuName
     * @param path
     */
    public MenuVo savePluginMenu(String pluginId, String menuName, String menuEnName, Integer order, String path) {
        return saveMenu(pluginId, menuName, menuEnName, order, path, 0, SysMenuPluginOpenWay.PAGE_OPEN.getCode(), SysMenuVisible.SHOW.getCode(), null, null);
    }

    /**
     * save menu and router
     * @param pluginId
     * @param menuName
     * @param parentId
     */
    public MenuVo savePluginMenu(String pluginId, String menuName, String menuEnName, Integer order, String path, Integer parentId) {
        return saveMenu(pluginId, menuName, menuEnName, order, path, parentId, SysMenuPluginOpenWay.PAGE_OPEN.getCode(), SysMenuVisible.SHOW.getCode(), null, null);
    }
    /**
     * save menu and router
     * @param pluginId
     * @param menuName
     * @param parentId
     * @param openWay
     */
    public MenuVo savePluginMenu(String pluginId, String menuName, String menuEnName, Integer order, String path, Integer parentId, Integer openWay) {
        return saveMenu(pluginId, menuName, menuEnName,order, path, parentId, openWay, SysMenuVisible.SHOW.getCode(), null, null);
    }

    /**
     * save router
     * @param pluginId
     * @param menuName
     * @param position 2 index instance
     */
    private MenuVo savePluginRoute(String pluginId, String menuName,String menuEnName, String path, Integer position,String queryTemplate) {
        return saveMenu(pluginId, menuName,menuEnName, 500, path, 0, SysMenuPluginOpenWay.PAGE_OPEN.getCode(), SysMenuVisible.HIDE.getCode(), position, queryTemplate);
    }

    /**
     * save router
     * @param pluginId
     * @param menuName
     * @param position 2 index instance
     */
    public MenuVo savePluginRoute(String pluginId, String menuName, String path, Integer parentId) {
        return saveMenu(pluginId, menuName,"", 500, path, parentId, SysMenuPluginOpenWay.PAGE_OPEN.getCode(), SysMenuVisible.HIDE.getCode(), SysMenuRouteOpenPosition.INDEX_INSTANCE_DATA.getCode(), null);
    }

    /**
     * save router
     * @param pluginId
     * @param menuName
     * @param position 2 index instance
     */
    public MenuVo savePluginRoute(String pluginId, String menuName,String menuEnName, String path, Integer parentId) {
        return saveMenu(pluginId, menuName,menuEnName, 500, path, parentId, SysMenuPluginOpenWay.PAGE_OPEN.getCode(), SysMenuVisible.HIDE.getCode(), SysMenuRouteOpenPosition.INDEX_INSTANCE_DATA.getCode(), null);
    }

    /**
     * save index router
     * @param pluginId
     * @param menuName
     * @param path
     * @return
     */
    public MenuVo saveIndexInstanceRoute(String pluginId, String menuName, String path) {
        String queryTemplate = "?id=${id}";
        return savePluginRoute(pluginId, menuName,"", path, SysMenuRouteOpenPosition.INDEX_INSTANCE_DATA.getCode(),queryTemplate);
    }

    /**
     * delete menu
     *
     * @param pluginId pluginId
     */
    public void deletePluginMenu(String pluginId) {
        sysMenuService.updateDisableByPluginId(pluginId);
    }
}
