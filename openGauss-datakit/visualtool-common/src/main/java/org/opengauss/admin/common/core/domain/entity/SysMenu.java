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
 * SysMenu.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/SysMenu.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.opengauss.admin.common.core.vo.MenuVo;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.*;

/**
 * menu permissions sys_menu
 *
 * @author xielibo
 */
@Data
public class SysMenu {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Integer menuId;

    /**
     * menu name
     */
    private String menuName;

    /**
     * menu en name
     */
    private String menuEnName;

    /**
     * parent name
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * parent id
     */
    private Integer parentId;

    /**
     * order
     */
    private Integer orderNum;

    /**
     * route path
     */
    private String path;

    /**
     * component
     */
    private String component;

    /**
     * is frame（0 yes 1 no）
     */
    private String isFrame;

    /**
     * is cache（0 yes 1 no）
     */
    private String isCache;

    /**
     * menu type（M directory C menu）
     */
    private String menuType;

    /**
     * visible（0:display 1:hide）
     */
    private String visible;

    /**
     * status（0:display 1:hide）
     */
    private String status;

    /**
     * perms
     */
    private String perms;

    /**
     * icon
     */
    private String icon;

    /**
     * open way 1：page；2：dialog；default:1
     */
    private Integer openWay;

    /**
     * Plugin ID
     */
    private String pluginId;

    /**
     *  plugin menu theme
     */
    private String pluginTheme;

    /**
     * openPosition 1：left menu bar ；2：Home Instance Block, default 1.
     */
    private Integer openPosition;

    /**
     * query parameter template
     */
    private String queryTemplate;


    /**
     * creater
     */
    private String createBy;

    /**
     * create time
     */
    private Date createTime;

    /**
     * updater
     */
    private String updateBy;

    /**
     * update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * remark
     */
    private String remark;

    /**
     * menu classify
     */
    private Integer menuClassify;

    /**
     * route query
     */
    @TableField(exist = false)
    private Map<String, Object> query;

    /**
     * request params
     */
    @TableField(exist = false)
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>(4);
        }
        return params;
    }

    /**
     * children
     */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<SysMenu>();


    @NotBlank(message = "menuName cannot be empty")
    @Size(min = 0, max = 50, message = "menuName length cannot exceed 50 characters")
    public String getMenuName() {
        return menuName;
    }


    @NotNull(message = "orderNum cannot be empty")
    public Integer getOrderNum() {
        return orderNum;
    }

    @NotBlank(message = "path cannot be empty")
    @Size(min = 0, max = 200, message = "path cannot exceed 200 characters")
    public String getPath() {
        return path;
    }


    @Size(min = 0, max = 200, message = "component cannot exceed 255 characters")
    public String getComponent() {
        return component;
    }

    @NotBlank(message = "menuType cannot be empty")
    public String getMenuType() {
        return menuType;
    }

    @Size(min = 0, max = 100, message = "perms cannot exceed 100 characters")
    public String getPerms() {
        return perms;
    }

    public MenuVo toMenuVo() {
        MenuVo vo = MenuVo.builder().menuId(menuId).menuName(menuName)
                .parentId(parentId).build();
        return vo;
    }
}
