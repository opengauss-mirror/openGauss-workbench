package org.opengauss.admin.common.core.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Router Info
 *
 * @author xielibo
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {
    /**
     * name
     */
    private String name;

    /**
     * route path
     */
    private String path;

    /**
     * redirect，when noRedirect is set the route is not clickable in breadcrumbs
     */
    private String redirect;

    /**
     * component
     */
    private String component;

    /**
     * when you declare more than one route under the children of a route, it will automatically become a nested mode - such as a component page
     */
    private Boolean alwaysShow;

    private Boolean isPluginMenu;

    private String menuTheme;

    private MetaVo meta;

    private Integer menuClassify;

    /**
     * childrens
     */
    private List<RouterVo> children;

    /**
     * route query
     */
    @TableField(exist = false)
    private Map<String, Object> query;
}
