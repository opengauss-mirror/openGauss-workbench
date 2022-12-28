package org.opengauss.admin.common.core.vo;

import lombok.Data;

/**
 * Menu Router Info
 *
 * @author xielibo
 */
@Data
public class MetaVo {
    /**
     * title
     */
    private String title;

    /**
     * icon
     */
    private String icon;

    private Integer order;

    private boolean requiresAuth;

    private boolean hideChildrenInMenu;

    /**
     * hideInMenu
     */
    private boolean hideInMenu;


    public MetaVo() {
    }

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.requiresAuth = false;
    }

    public MetaVo(String title, String icon, boolean noCache,Integer order,boolean hideChildrenInMenu,boolean hideInMenu) {
        this.title = title;
        this.icon = icon;
        this.order = order;
        this.requiresAuth = false;
        this.hideChildrenInMenu = hideChildrenInMenu;
        this.hideInMenu = hideInMenu;
    }
}
